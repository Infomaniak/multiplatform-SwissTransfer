/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2024-2026 Infomaniak Network SA
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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.infomaniak.multiplatform_swisstransfer.managers

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.CrashReportInterface
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.TransferUi
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.TransferUi.ApiSource.V1
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.TransferUi.ApiSource.V2
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.common.utils.mapToList
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtilsForApiV2
import com.infomaniak.multiplatform_swisstransfer.database.utils.isExpectedRealmError
import com.infomaniak.multiplatform_swisstransfer.exceptions.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.exceptions.NullPropertyException
import com.infomaniak.multiplatform_swisstransfer.mappers.toTransferUi
import com.infomaniak.multiplatform_swisstransfer.mappers.toTransferUiList
import com.infomaniak.multiplatform_swisstransfer.mappers.toTransferUiListFlow
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiV2ErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.DownloadQuotaExceededException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.DownloadLimitReached
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.ExpiredDateFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.NotFoundFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.PasswordNeededFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.TransferCancelledException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.VirusCheckFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.VirusDetectedFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.WrongPasswordFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnauthorizedException
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferV2Repository
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiUrlMatcher.isV2Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.v2.TransferApi as TransferApiV2

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
    private val crashReport: CrashReportInterface,
    private val transferController: TransferController,
    private val transferRepository: TransferRepository,
    private val transferV2Repository: TransferV2Repository,
) {

    private val minDurationBetweenAutoUpdates = 15.minutes
    private var lastUpdateDate: TimeMark = TimeSource.Monotonic.markNow() - (minDurationBetweenAutoUpdates + 1.seconds)

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
    fun getAllTransfers(): Flow<List<TransferUi>> = userDependentFlow(
        flowForAuthUser = { userId -> transferDao.transfersFlow(userId).toTransferUiListFlow(transferDao) },
        flowForGuestUser = { transferController.getAllTransfersFlow().map { it.mapToList(::TransferUi) } },
        merge = { authTransfers, guestTransfers -> authTransfers + guestTransfers }
    ).catchDbExceptions()

    /**
     * Retrieves a flow of transfers based on the specified transfer direction.
     *
     * @see addTransferByUrl
     * @see addTransferByLinkUUID
     *
     * @param transferDirection The direction of the transfers to retrieve (e.g., [TransferDirection.SENT] or [TransferDirection.RECEIVED]).
     * @return A `Flow` that emits a list of transfers matching the specified direction.
     */
    fun getSortedTransfers(transferDirection: TransferDirection): Flow<SortedTransfers> = userDependentFlow(
        flowForAuthUser = { userId ->
            combine(
                transferDao.validTransfersFlow(userId, transferDirection),
                transferDao.expiredTransfersFlow(userId, transferDirection)
            ) { valid, expired ->
                SortedTransfers(
                    valid.toTransferUiList(transferDao),
                    expired.toTransferUiList(transferDao)
                )
            }
        },
        flowForGuestUser = {
            combine(
                transferController.getValidTransfersFlow(transferDirection),
                transferController.getExpiredTransfersFlow(transferDirection)
            ) { valid, expired ->
                SortedTransfers(valid.mapToList(::TransferUi), expired.mapToList(::TransferUi))
            }
        },
        merge = { a, b -> a + b }
    ).catchDbExceptions()

    fun getTransfersCount(transferDirection: TransferDirection): Flow<Long> = userDependentFlow(
        flowForAuthUser = { userId -> transferDao.transfersCountFlow(userId, transferDirection).map { it.toLong() } },
        flowForGuestUser = { transferController.getTransfersCountFlow(transferDirection) },
        merge = { a, b -> a + b }
    ).catchDbExceptions()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransferFlow(transferUUID: String): Flow<TransferUi?> = userDependentFlow(
        flowForAuthUser = { userId ->
            transferDao.transferFlow(userId, transferUUID).mapLatest { transferDB -> transferDB?.toTransferUi(transferDao) }
        },
        flowForGuestUser = { transferController.getTransferFlow(transferUUID).map { transfer -> transfer?.let(::TransferUi) } },
        merge = { a, b -> a ?: b }
    ).catchDbExceptions()

    /**
     * Fetch all transfers in database to update their status
     *
     * This function tries to update all existing transfers if it's been more than [minDurationBetweenAutoUpdates] since the last
     * auto update.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun tryUpdatingAllTransfers(): Unit = withContext(Dispatchers.Default) {
        if (!showGuestData()) {
            //TODO[API-V2] Do it for v2 transfers too when needed.
            // We probably don't need it yet since we don't have download count yet,
            // and `fetchWaitingTransfers` (below) already updates v2 transfers for the virus check related statuses.
            return@withContext
        }

        val nextUpdate = lastUpdateDate + minDurationBetweenAutoUpdates
        if (nextUpdate.hasNotPassedNow()) return@withContext

        val semaphore = Semaphore(4)

        coroutineScope {
            transferController.getAllTransfers().forEach { transfer ->
                launch {
                    semaphore.withPermit {
                        runCatching {
                            fetchV1Transfer(transfer.linkUUID)
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
        when (val userId = currentUserId) {
            null -> Unit
            else -> launch {
                transferDao.getUploadedButNotReadyTransfers(userId).forEach { transfer ->
                    launch {
                        runCatching {
                            fetchV2Transfer(transfer)
                        }.onFailure { exception ->
                            if (exception is CancellationException) throw exception
                        }
                    }
                }
            }
        }
        if (!showGuestData()) return@withContext
        transferController.getNotReadyTransfers().forEach { transfer ->
            runCatching {
                fetchV1Transfer(transfer.linkUUID)
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception
            }
        }
    }

    /**
     * Designed to keep a reference to the id from Android's DownloadManager.
     */
    suspend fun writeDownloadManagerId(
        transfer: TransferUi,
        fileUid: String?,
        uniqueDownloadManagerId: Long?,
    ) {
        val transferId = transfer.uuid
        when (transfer.apiSource) {
            V1 -> transferController.writeDownloadManagerId(transferId, fileUid, uniqueDownloadManagerId)
            V2 -> writeDownloadManagerForV2(transferId, fileUid, uniqueDownloadManagerId)
        }
    }

    /**
     * Persists references to Android's DownloadManager IDs or removes them if the download is cancelled.
     *
     * For each [DownloadManagerArgs] in the list:
     * - If [DownloadManagerArgs.uniqueDownloadManagerId] is null, deletes the reference
     * - Otherwise, inserts or updates the reference with the new ID
     *
     * The list is chunked into batches of [batchLimit] to avoid blocking the database writer for too long.
     *
     * @param downloadManagerArgs List of transfer/file/downloadID mappings to persist.
     *                            Processed in chunks of [batchLimit] to maintain UI responsiveness.
     *
     * @throws IllegalArgumentException If batchLimit is smaller than 0 or equals to 0
     * @throws IllegalStateException If called when no user is logged in.
     */
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    suspend fun writeDownloadManagerIdsForV2(
        downloadManagerArgs: List<DownloadManagerArgs>,
        batchLimit: Int = 500,
    ) {
        require(batchLimit > 0) { "batchLimit must be greater than 0" }
        if (currentUserId == null) error("No user logged in")
        downloadManagerArgs.chunked(batchLimit).forEach { batch ->
            appDatabase.useWriterConnection {
                it.immediateTransaction {
                    batch.forEach { args ->
                        writeDownloadManagerForV2(args.transferId, args.fileId, args.uniqueDownloadManagerId)
                    }
                }
            }
        }
    }

    /**
     * Gives the id to retrieve a previous download assigned to Android's DownloadManager.
     */
    fun downloadManagerIdFor(transfer: TransferUi, fileUid: String?): Flow<Long?> {
        //TODO[ST-v2]: Maybe migrate all that data from Realm, to avoid this condition.
        return when (transfer.apiSource) {
            V1 -> transferController.downloadManagerIdFor(transfer.uuid, fileUid)
            V2 -> appDatabase.getDownloadManagerRef().getDownloadManagerId(transferId = transfer.uuid, fileId = fileUid ?: "")
        }
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
        when (val transfer = transferDao.getTransfer(transferId = transferUUID)) {
            null -> fetchV1Transfer(transferUUID)
            else -> fetchV2Transfer(transfer)
        }
    }

    /**
     * Update the local transfer with remote API
     */
    private suspend fun fetchV1Transfer(transferUUID: String) {
        val transfer = transferController.getTransfer(transferUUID)
            ?: throw NotFoundException("No transfer found in DB with uuid = $transferUUID")
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

    /**
     * Update the local transfer with remote API
     */
    private suspend fun fetchV2Transfer(transfer: TransferDB) {
        val transferId = transfer.id
        runCatching {
            val transferLinkId = transfer.linkId ?: return
            transferV2Repository.getTransferByLinkUUID(transferLinkId, transfer.password) // Check the transfer on the backend.
            if (transfer.transferStatus != TransferStatus.READY) {
                transferDao.updateStatus(transferId, TransferStatus.READY)
            }
        }.onFailure { exception ->
            val newStatus: TransferStatus = when (exception) {
                is VirusCheckFetchTransferException -> TransferStatus.WAIT_VIRUS_CHECK
                is VirusDetectedFetchTransferException -> TransferStatus.VIRUS_DETECTED
                is ExpiredDateFetchTransferException, is NotFoundFetchTransferException -> TransferStatus.EXPIRED_DATE
                else -> throw exception
            }
            transferDao.updateStatus(transferId, newStatus)
        }
    }

    suspend fun updateTransferFilesThumbnails(transferUUID: String, thumbnailRootPath: String) {
        when (transferDao.getTransfer(transferUUID)) {
            null -> transferController.updateTransferFilesThumbnails(transferUUID, thumbnailRootPath)
            else -> appDatabase.useWriterConnection {
                it.immediateTransaction {
                    transferDao.getTransferFiles(transferUUID).forEach { file ->
                        val fileDB = file.copy(thumbnailPath = "$thumbnailRootPath/${file.id}")
                        transferDao.upsertFile(fileDB)
                    }
                }
            }
        }
    }

    /**
     * Retrieves a [TransferUi] by linkUUID.
     *
     * @param transferUUID The UUID of an existing transfer in the database.
     *
     * @return A transfer matching the specified transferUUID or null.
     */
    suspend fun getTransferByUUID(transferUUID: String): TransferUi? {
        val transferUiFromV2 = currentUserId?.let { userId ->
            transferDao.transferFlow(userId, transferUUID).first()?.toTransferUi(transferDao)
        }
        return transferUiFromV2 ?: transferController.getTransfer(transferUUID)?.let(::TransferUi)
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
     * Retrieves a transfer using the provided link ID (API V2) and saves it to the database.
     *
     * This function fetches a transfer from the API v2 using the `linkId`, and after the transfer
     * is successfully retrieved, it is saved to the database as a received transfer.
     *
     * @param linkId The link ID used to identify the transfer to retrieve.
     * @param password The password for the transfer, if it is password protected.
     *
     * @return The transferId of the added transfer
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiV2ErrorException If there is an error related to the API v2 during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     * @throws UnauthorizedException If the request is unauthorized.
     * @throws PasswordNeededFetchTransferException If the transfer requires a password but none was provided.
     * @throws WrongPasswordFetchTransferException If the provided password is incorrect.
     * @throws NotFoundFetchTransferException If the transfer was not found.
     * @throws TransferCancelledException If the transfer has been cancelled.
     * @throws ExpiredDateFetchTransferException If the transfer is expired.
     * @throws DownloadLimitReached If the download limit has been reached.
     * @throws VirusCheckFetchTransferException If the virus check is in progress.
     * @throws VirusDetectedFetchTransferException If a virus has been detected.
     */
    @Throws(
        CancellationException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        UnauthorizedException::class,
        PasswordNeededFetchTransferException::class,
        WrongPasswordFetchTransferException::class,
        NotFoundFetchTransferException::class,
        TransferCancelledException::class,
        ExpiredDateFetchTransferException::class,
        DownloadLimitReached::class,
        VirusCheckFetchTransferException::class,
        VirusDetectedFetchTransferException::class,
    )
    suspend fun addTransferByLinkIdApiV2(
        linkId: String,
        password: String?,
    ): String = withContext(Dispatchers.Default) {
        val transferApi = transferV2Repository.getTransferByLinkUUID(linkId, password).transfer
        // TODO[Api-v2]: Handle download credit once it is available on API v2
        addTransferV2(linkId, transferApi, password)
        return@withContext transferApi.id
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
     * @throws ApiV2ErrorException
     * @throws DownloadLimitReached
     * @throws TransferCancelledException
     * @throws UnauthorizedException
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
        ApiV2ErrorException::class,
        DownloadLimitReached::class,
        TransferCancelledException::class,
        UnauthorizedException::class,
    )
    suspend fun addTransferByUrl(url: String, password: String? = null): String? = withContext(Dispatchers.Default) {
        if (isV2Url(url)) {
            val linkUUID = extractLinkUUIDFromURL(url)
            if (linkUUID != null) {
                val transferApi = transferV2Repository.getTransferByUrl(url, password).transfer
                if (transferDao.getTransfer(transferApi.id) == null) {
                    addTransferV2(linkUUID, transferApi, password)
                }
                return@withContext transferApi.id
            }
        }

        val transferApi = transferRepository.getTransferByUrl(url, password).data ?: return@withContext null
        transferApi.validateDownloadCounterCreditOrThrow()
        val transfer = transferController.getTransfer(transferApi.linkUUID)
        if (transfer == null) {
            addTransfer(transferApi, TransferDirection.RECEIVED, password)
        }
        return@withContext transferApi.linkUUID
    }

    private fun extractLinkUUIDFromURL(url: String): String? {
        val maybeLinkUUID = url.substringAfterLast('/')
        return maybeLinkUUID.takeIf { it.isNotEmpty() }
    }

    private suspend fun addTransferV2(linkId: String, transferApi: TransferApiV2, password: String?) {
        val userId = currentUserId ?: STUser.GuestUser.id
        val transferDB = TransferDB(
            transfer = transferApi,
            linkId = linkId,
            userOwnerId = userId,
            direction = TransferDirection.RECEIVED,
            password = password,
        )

        val filesToInsert = FileUtilsForApiV2.getFileDbTree(
            transferId = transferApi.id,
            files = transferApi.files
        )

        appDatabase.useWriterConnection {
            it.immediateTransaction {
                transferDao.upsertTransfer(transferDB)
                filesToInsert.forEach { file -> transferDao.upsertFile(file) }
            }
        }
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
        when (val transfer = transferDao.getTransfer(transferUUID)) {
            null -> transferController.deleteTransfer(transferUUID)
            else -> transferDao.deleteTransfer(transfer)
        }
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
        currentUserId?.let { transferDao.deleteExpiredTransfers() }
        if (showGuestData()) transferController.deleteExpiredTransfers()
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

    private suspend fun writeDownloadManagerForV2(transferId: String, fileUid: String?, uniqueDownloadManagerId: Long?) {
        if (uniqueDownloadManagerId == null) {
            appDatabase.getDownloadManagerRef().delete(
                transferId = transferId,
                fileId = fileUid ?: "",
            )
        } else {
            appDatabase.getDownloadManagerRef().update(
                DownloadManagerRefV2(
                    transferId = transferId,
                    fileId = fileUid ?: "",
                    downloadManagerUniqueId = uniqueDownloadManagerId,
                    userOwnerId = currentUserId ?: return
                )
            )
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

    private suspend fun showGuestData(): Boolean = accountManager.showGuestData.first()

    private fun <T> Flow<T>.catchDbExceptions() = catch { throwable ->
        if (throwable.isExpectedRealmError().not()) {
            crashReport.capture("Failure to load transfers", throwable)
        }
    }

    private fun <T> userDependentFlow(
        flowForAuthUser: (userId: Long) -> Flow<T>,
        flowForGuestUser: () -> Flow<T>,
        merge: suspend (authUserData: T, guestUserData: T) -> T,
    ): Flow<T> = accountManager.currentUserFlow.flatMapLatest { currentUser ->
        when (currentUser) {
            is STUser.AuthUser -> {
                accountManager.shouldShowGuestData(currentUser).flatMapLatest { shouldShowGuestData ->
                    when {
                        shouldShowGuestData -> {
                            combine(
                                flowForAuthUser(currentUser.id),
                                flowForGuestUser()
                            ) { authUserData, guestUserData -> merge(authUserData, guestUserData) }
                        }
                        else -> flowForAuthUser(currentUser.id)
                    }
                }
            }
            STUser.GuestUser -> flowForGuestUser()
            null -> emptyFlow()
        }
    }

    data class SortedTransfers(
        val validTransfers: List<TransferUi>,
        val expiredTransfers: List<TransferUi>,
    ) {
        internal operator fun plus(other: SortedTransfers): SortedTransfers {
            return SortedTransfers(
                validTransfers = validTransfers + other.validTransfers,
                expiredTransfers = expiredTransfers + other.expiredTransfers
            )
        }
    }

    data class DownloadManagerArgs(val transferId: String, val fileId: String?, val uniqueDownloadManagerId: Long?)
}
