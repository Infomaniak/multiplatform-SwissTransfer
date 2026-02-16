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
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.exceptions.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.exceptions.NullPropertyException
import com.infomaniak.multiplatform_swisstransfer.mappers.toTransferUi
import com.infomaniak.multiplatform_swisstransfer.mappers.toTransferUiList
import com.infomaniak.multiplatform_swisstransfer.mappers.toTransferUiListFlow
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.DownloadQuotaExceededException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.ExpiredDateFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.NotFoundFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.PasswordNeededFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.VirusCheckFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.VirusDetectedFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.WrongPasswordFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferV2Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.DownloadManagerRef as DownloadManagerRefV2

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
    private val accountManager: AccountManager,
    private val appDatabase: AppDatabase,
    private val transferController: TransferController,
    private val transferRepository: TransferRepository,
    private val transferV2Repository: TransferV2Repository,
) {

    private val minDurationBetweenAutoUpdates = 15.minutes
    private var lastUpdateDate: TimeMark = TimeSource.Monotonic.markNow() - (minDurationBetweenAutoUpdates + 1.seconds)

    //TODO[ST-v2]: Use a flow for currentUserId
    private val currentUserId: Long?
        get() = (accountManager.currentUser as? STUser.AuthUser)?.id

    private val transferDao get() = appDatabase.getTransferDao()

    /**
     * Retrieves a flow of all transfers.
     *
     * @see addTransferByUrl
     * @see addTransferByLinkUUID
     *
     * @return A `Flow` that emits the list of all transfers.
     */
    @Throws(RealmException::class)
    fun getAllTransfers(): Flow<List<TransferUi>> {
        currentUserId?.let { userId ->
            return transferDao.getTransfers(userId).toTransferUiListFlow(transferDao)
        }
        return transferController.getAllTransfersFlow().map { it.mapToList(::TransferUi) }
    }

    /**
     * Retrieves a flow of transfers based on the specified transfer direction.
     *
     * @see addTransferByUrl
     * @see addTransferByLinkUUID
     *
     * @param transferDirection The direction of the transfers to retrieve (e.g., [TransferDirection.SENT] or [TransferDirection.RECEIVED]).
     * @return A `Flow` that emits a list of transfers matching the specified direction.
     */
    @Throws(RealmException::class)
    fun getSortedTransfers(transferDirection: TransferDirection): Flow<SortedTransfers> {
        currentUserId?.let { userId ->
            return transferDao.getValidTransfers(userId, transferDirection)
                .combine(transferDao.getExpiredTransfers(userId, transferDirection)) { valid, expired ->
                    SortedTransfers(
                        valid.toTransferUiList(transferDao),
                        expired.toTransferUiList(transferDao)
                    )
                }
        }
        return transferController.getValidTransfersFlow(transferDirection)
            .combine(transferController.getExpiredTransfersFlow(transferDirection)) { valid, expired ->
                SortedTransfers(valid.mapToList(::TransferUi), expired.mapToList(::TransferUi))
            }
    }

    @Throws(RealmException::class)
    fun getTransfersCount(transferDirection: TransferDirection): Flow<Long> {
        currentUserId?.let { userId ->
            return transferDao.getTransfersCount(userId, transferDirection).map { it.toLong() }
        }
        return transferController.getTransfersCountFlow(transferDirection)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Throws(RealmException::class)
    fun getTransferFlow(transferUUID: String): Flow<TransferUi?> {
        currentUserId?.let { userId ->
            return transferDao.getTransfer(userId, transferUUID).mapLatest { transferDB ->
                transferDB?.toTransferUi(transferDao)
            }
        }
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
        if (currentUserId != null) return@withContext //TODO[API-V2] Handle transfer status

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
        if (currentUserId != null) return@withContext //TODO[API-V2] Handle transfer status
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
        currentUserId?.let {
            val transfer = transferDao.getTransfer(userId = it, transferId = transferUUID).first()
            if (transfer != null) {
                if (uniqueDownloadManagerId == null) {
                    appDatabase.getDownloadManagerRef().delete(
                        transferId = transferUUID,
                        fileId = fileUid ?: ""
                    )
                } else {
                    appDatabase.getDownloadManagerRef().update(
                        DownloadManagerRefV2(
                            transferId = transferUUID,
                            fileId = fileUid ?: "",
                            downloadManagerUniqueId = uniqueDownloadManagerId,
                            userOwnerId = it
                        )
                    )
                }
                return
            }
        }
        transferController.writeDownloadManagerId(transferUUID, fileUid, uniqueDownloadManagerId)
    }

    /**
     * Gives the id to retrieve a previous download assigned to Android's DownloadManager.
     */
    fun downloadManagerIdFor(transferUUID: String, fileUid: String?): Flow<Long?> {
        //TODO: Maybe migrate all that data from Realm, to avoid having to merge the 2 flows.
        currentUserId?.let {
            return appDatabase.getDownloadManagerRef().getDownloadManagerId(transferId = transferUUID, fileId = fileUid ?: "")
        }
        return transferController.downloadManagerIdFor(transferUUID, fileUid)
    }

    /**
     * Update the local transfer with remote api
     *
     * @throws RealmException An error has occurred with realm database
     * @throws NotFoundException Any transfer with [transferUUID] has been found
     * @throws NullPropertyException The transferDirection of the transfer found is null
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     *
     */
    @Throws(
        RealmException::class,
        NotFoundException::class,
        NullPropertyException::class,
        UnexpectedApiErrorFormatException::class,
        CancellationException::class,
        ApiErrorException::class,
        NetworkException::class,
        UnknownException::class,
        PasswordNeededFetchTransferException::class,
        WrongPasswordFetchTransferException::class,
    )
    suspend fun fetchTransfer(transferUUID: String) {
        currentUserId?.let {
            val transfer = transferDao.getTransfer(userId = it, transferUUID).first() ?: return
            val transferLinkId = transfer.linkId ?: return
            val remoteTransfer = transferV2Repository.getTransferByLinkUUID(transferLinkId, transfer.password)
            //TODO[ST-v2]: Update transfer status here
            return
        }
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

    //TODO[ST-v2]: Check if this method is still used
    suspend fun updateTransferFilesThumbnails(transferUUID: String, thumbnailRootPath: String) {
        currentUserId?.let {
            val transfer = transferDao.getTransfer(userId = it, transferUUID).first() ?: return
            //TODO[ST-v2]: Optimize with One transaction instead
            transferDao.getTransferFiles(transferUUID).forEach { it ->
                val fileDB = it.copy(thumbnailPath = "$thumbnailRootPath/${it.id}")
                transferDao.upsertFile(fileDB)
            }
            return
        }
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
        currentUserId?.let { userId ->
            return transferDao.getTransfer(userId, transferUUID).first()?.toTransferUi(transferDao)
        }
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
     * @see getAllTransfers
     * @see getSortedTransfers
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
     * @see getAllTransfers
     * @see getSortedTransfers
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
        currentUserId?.let { userId ->
            val transfer = transferDao.getTransfer(userId, transferUUID).first() ?: return@withContext
            transferDao.deleteTransfer(transfer)
            return@withContext
        }
        transferController.deleteTransfer(transferUUID)
    }

    /**
     * Delete a transfer by deeplink.
     *
     * @param transferUUID The UUID of the transfer to be removed.
     * @param token Delete token.
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiErrorException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     * @throws RealmException An error has occurred with realm database
     */
    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class
    )
    suspend fun deleteTransfer(transferUUID: String, token: String): Unit = withContext(Dispatchers.Default) {
        transferRepository.delete(transferUUID, token)
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
        currentUserId?.let {
            transferDao.deleteExpiredTransfers()
            return@withContext
        }
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

    internal suspend fun createTransferLocally(
        linkUUID: String,
        uploadSession: UploadSession,
        transferStatus: TransferStatus
    ) {
        runCatching {
            transferController.generateAndInsert(linkUUID, uploadSession, transferStatus)
        }.onFailure {
            throw UnknownException(it)
        }
    }

    data class SortedTransfers(
        val validTransfers: List<TransferUi>,
        val expiredTransfers: List<TransferUi>,
    )
}
