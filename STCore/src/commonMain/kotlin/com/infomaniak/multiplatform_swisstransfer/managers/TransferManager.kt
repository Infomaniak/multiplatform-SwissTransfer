/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.multiplatform_swisstransfer.managers

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.TransferUi
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.common.utils.mapToList
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.exceptions.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.exceptions.NullPropertyException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.DownloadQuotaExceededException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.*
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * TransferManager is responsible for orchestrating data transfer operations
 * using Realm for local data management and an API client for network communications.
 *
 * This class integrates with both RealmProvider and ApiClientProvider to ensure
 * smooth and efficient data transfers, providing a centralized management point
 * for transfer-related activities.
 *
 * @property transferController The provider for transfer data from database.
 * @property transferRepository The provider for transfer data from api.
 */
class TransferManager internal constructor(
    private val transferController: TransferController,
    private val transferRepository: TransferRepository,
) {

    private val minDurationBetweenAutoUpdates = 15.minutes
    private var lastUpdateDate: TimeMark = TimeSource.Monotonic.markNow() - (minDurationBetweenAutoUpdates + 1.seconds)

    /**
     * Retrieves a flow of transfers based on the specified transfer direction.
     *
     * @see addTransferByUrl
     * @see addTransferByLinkUUID
     *
     * @param transferDirection The direction of the transfers to retrieve (e.g., [TransferDirection.SENT] or [TransferDirection.RECEIVED]) or null to get both.
     * @return A `Flow` that emits a list of transfers matching the specified direction.
     */
    @Throws(RealmException::class)
    fun getTransfers(transferDirection: TransferDirection? = null): Flow<List<TransferUi>> {
        return transferController.getTransfersFlow(transferDirection)
            .map { it.mapToList { transfer -> TransferUi(transfer) } }
    }

    @Throws(RealmException::class)
    fun getTransfersCount(transferDirection: TransferDirection): Flow<Long> {
        return transferController.getTransfersCountFlow(transferDirection)
    }

    @Throws(RealmException::class)
    fun getTransferFlow(transferUUID: String): Flow<TransferUi?> {
        return transferController.getTransferFlow(transferUUID)
            .map { transfer -> transfer?.let(::TransferUi) }
    }

    /**
     * Fetch all transfers in database to update their status
     *
     * This function tries to update all existing transfers if it's been more than [minDurationBetweenAutoUpdates] since the last
     * auto update.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun tryUpdatingAllTransfers(): Unit = withContext(Dispatchers.Default) {
        val nextUpdate = lastUpdateDate + minDurationBetweenAutoUpdates
        if (nextUpdate.hasNotPassedNow()) return@withContext

        val semaphore = Semaphore(4)

        coroutineScope {
            transferController.getAllTransfers().forEach { transfer ->
                launch {
                    semaphore.withPermit {
                        runCatching {
                            fetchTransfer(transfer)
                        }.onFailure { exception ->
                            if (exception is CancellationException) throw exception
                        }
                    }
                }
            }
        }

        lastUpdateDate = TimeSource.Monotonic.markNow()
    }

    /**
     * Update all pending transfers in database, most transfers are in [TransferStatus.WAIT_VIRUS_CHECK] status.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun fetchWaitingTransfers(): Unit = withContext(Dispatchers.Default) {
        transferController.getNotReadyTransfers().forEach { transfer ->
            runCatching {
                fetchTransfer(transfer)
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception
            }
        }
    }

    /**
     * Designed to keep a reference to the id from Android's DownloadManager.
     */
    suspend fun writeDownloadManagerId(
        transferUUID: String,
        fileUid: String?,
        uniqueDownloadManagerId: Long?
    ) {
        transferController.writeDownloadManagerId(transferUUID, fileUid, uniqueDownloadManagerId)
    }

    /**
     * Gives the id to retrieve a previous download assigned to Android's DownloadManager.
     */
    fun downloadManagerIdFor(transferUUID: String, fileUid: String?): Flow<Long?> {
        return transferController.downloadManagerIdFor(transferUUID, fileUid)
    }

    /**
     * Update the local transfer with remote api
     *
     * @throws RealmException An error has occurred with realm database
     * @throws NotFoundException Any transfer with [transferUUID] has been found
     * @throws NullPropertyException The transferDirection of the transfer found is null
     */
    @Throws(
        RealmException::class,
        NotFoundException::class,
        NullPropertyException::class,
        CancellationException::class,
        ApiErrorException::class,
        NetworkException::class,
        UnknownException::class,
        PasswordNeededFetchTransferException::class,
        WrongPasswordFetchTransferException::class,
    )
    suspend fun fetchTransfer(transferUUID: String) {
        val localTransfer = transferController.getTransfer(transferUUID)
            ?: throw NotFoundException("No transfer found in DB with uuid = $transferUUID")

        fetchTransfer(localTransfer)
    }

    /**
     * Update the local transfer with remote api
     */
    private suspend fun fetchTransfer(transfer: Transfer) {
        runCatching {
            val remoteTransfer = transferRepository.getTransferByLinkUUID(transfer.linkUUID, transfer.password).data ?: return
            transferController.update(remoteTransfer)
        }.onFailure { exception ->
            when (exception) {
                is VirusCheckFetchTransferException -> transferController.updateTransferStatus(
                    transfer.linkUUID,
                    TransferStatus.WAIT_VIRUS_CHECK,
                )
                is VirusDetectedFetchTransferException -> transferController.updateTransferStatus(
                    transfer.linkUUID,
                    TransferStatus.VIRUS_DETECTED,
                )
                is ExpiredDateFetchTransferException, is NotFoundFetchTransferException -> transferController.updateTransferStatus(
                    transfer.linkUUID,
                    TransferStatus.EXPIRED_DATE,
                )
                else -> throw exception
            }
        }
    }

    suspend fun updateTransferFilesThumbnails(transferUUID: String, thumbnailRootPath: String) {
        transferController.updateTransferFilesThumbnails(transferUUID, thumbnailRootPath)
    }

    /**
     * Retrieves a [TransferUi] by linkUUID.
     *
     * @param transferUUID The UUID of an existing transfer in the database.
     *
     * @return A transfer matching the specified transferUUID or null.
     */
    suspend fun getTransferByUUID(transferUUID: String): TransferUi? {
        return transferController.getTransfer(transferUUID)?.let(::TransferUi)
    }

    /**
     * Retrieves a transfer using the provided link UUID and saves it to the database.
     *
     * This function is typically used after a transfer has been uploaded. Once the upload is complete,
     * a `linkUUID` is returned, which must be passed to this function to retrieve the corresponding transfer.
     * After retrieving the transfer, it is saved to the database.
     *
     * Some data are not part of the API and we need to save them manually like [password] and [recipientsEmails].
     *
     * @see getTransfers
     *
     * @param linkUUID The UUID corresponding to the uploaded transfer link or transferUUID.
     * @param password The transfer password if protected
     * @param recipientsEmails The transfer recipients emails if transfer type is "email" else the list can be empty
     * @param transferDirection The direction of the transfers to retrieve (e.g., [TransferDirection.SENT])
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiErrorException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     * @throws RealmException An error has occurred with realm database
     * @throws VirusCheckFetchTransferException If the virus check is in progress
     * @throws VirusDetectedFetchTransferException If a virus has been detected
     * @throws ExpiredDateFetchTransferException If the transfer is expired
     * @throws DownloadQuotaExceededException If the transfer was downloaded too many times
     * @throws NotFoundFetchTransferException If the transfer doesn't exist
     * @throws PasswordNeededFetchTransferException If the transfer is protected by a password
     * @throws WrongPasswordFetchTransferException If we entered a wrong password for a transfer
     */
    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
        VirusCheckFetchTransferException::class,
        VirusDetectedFetchTransferException::class,
        ExpiredDateFetchTransferException::class,
        DownloadQuotaExceededException::class,
        NotFoundFetchTransferException::class,
        PasswordNeededFetchTransferException::class,
        WrongPasswordFetchTransferException::class,
    )
    suspend fun addTransferByLinkUUID(
        linkUUID: String,
        password: String?,
        recipientsEmails: Set<String> = emptySet(),
        transferDirection: TransferDirection,
    ): Unit = withContext(Dispatchers.Default) {
        val transferApi = transferRepository.getTransferByLinkUUID(linkUUID, password).data ?: return@withContext
        transferApi.validateDownloadCounterCreditOrThrow()

        addTransfer(transferApi, transferDirection, password, recipientsEmails)
    }

    /**
     * Retrieves a transfer using the provided URL and saves it to the database.
     *
     * This function is used when a transfer URL is available. The provided `url` is used to retrieve
     * the corresponding transfer, and after the transfer is successfully retrieved, it is saved to
     * the database.
     *
     * @see getTransfers
     *
     * @param url The URL associated with the transfer to retrieve.
     *
     * @return The transferUUID of the added transfer, otherwise null if the api doesn't return the transfer
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiErrorException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     * @throws RealmException An error has occurred with realm database
     * @throws VirusCheckFetchTransferException If the virus check is in progress
     * @throws VirusDetectedFetchTransferException If a virus has been detected
     * @throws ExpiredDateFetchTransferException If the transfer is expired
     * @throws DownloadQuotaExceededException If the transfer was downloaded too many times
     * @throws NotFoundFetchTransferException If the transfer doesn't exist
     * @throws PasswordNeededFetchTransferException If the transfer is protected by a password
     * @throws WrongPasswordFetchTransferException If we entered a wrong password for a transfer
     */
    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
        VirusCheckFetchTransferException::class,
        VirusDetectedFetchTransferException::class,
        ExpiredDateFetchTransferException::class,
        DownloadQuotaExceededException::class,
        NotFoundFetchTransferException::class,
        PasswordNeededFetchTransferException::class,
        WrongPasswordFetchTransferException::class,
    )
    suspend fun addTransferByUrl(url: String, password: String? = null): String? = withContext(Dispatchers.Default) {
        val transferApi = transferRepository.getTransferByUrl(url, password).data ?: return@withContext null
        transferApi.validateDownloadCounterCreditOrThrow()
        val transfer = transferController.getTransfer(transferApi.linkUUID)
        if (transfer == null) {
            addTransfer(transferApi, TransferDirection.RECEIVED, password)
        }
        return@withContext transferApi.linkUUID
    }

    private fun TransferApi.validateDownloadCounterCreditOrThrow() {
        if (downloadCounterCredit <= 0) throw DownloadQuotaExceededException()
    }

    /**
     * Delete a transfer by its UUID.
     *
     * @param transferUUID The UUID of the transfer to be removed.
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws RealmException An error has occurred with realm database
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun deleteTransfer(transferUUID: String): Unit = withContext(Dispatchers.Default) {
        transferController.deleteTransfer(transferUUID)
    }

    /**
     * Delete Transfers that are expired since a certain amount of days.
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws RealmException An error has occurred with realm database
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun deleteExpiredTransfers() = withContext(Dispatchers.Default) {
        transferController.deleteExpiredTransfers()
    }

    private suspend fun addTransfer(
        transferApi: TransferApi?,
        transferDirection: TransferDirection,
        password: String?,
        recipientsEmails: Set<String> = emptySet(),
    ) {
        runCatching {
            transferController.insert(transferApi as Transfer, transferDirection, password, recipientsEmails)
        }.onFailure {
            throw UnknownException(it)
        }
    }

    internal suspend fun createTransferLocally(linkUUID: String, uploadSession: UploadSession, transferStatus: TransferStatus) {
        runCatching {
            transferController.generateAndInsert(linkUUID, uploadSession, transferStatus)
        }.onFailure {
            throw UnknownException(it)
        }
    }
}
