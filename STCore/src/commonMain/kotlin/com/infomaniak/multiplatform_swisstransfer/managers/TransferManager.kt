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
import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.TransferUi
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.data.STUser.GuestUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtilsForApiV2
import com.infomaniak.multiplatform_swisstransfer.exceptions.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.exceptions.NullPropertyException
import com.infomaniak.multiplatform_swisstransfer.exceptions.UnsupportedTransferDeeplinkUrlException
import com.infomaniak.multiplatform_swisstransfer.mappers.toTransferDB
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
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferV2Repository
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiUrlMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
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
 * using Room for local data management and an API client for network communications.
 */
class TransferManager internal constructor(
    private val accountManager: AccountManager,
    private val appDatabase: AppDatabase,
    private val transferV2Repository: TransferV2Repository,
) {

    private val minDurationBetweenAutoUpdates = 15.minutes
    private var lastUpdateDate: TimeMark = TimeSource.Monotonic.markNow() - (minDurationBetweenAutoUpdates + 1.seconds)

    private val transferDao get() = appDatabase.getTransferDao()

    /**
     * Retrieves a flow of all transfers.
     *
     * @see addTransferByUrl
     * @see addTransferByLinkIdApiV2
     *
     * @return A `Flow` that emits the list of all transfers, regardless of the selected user.
     */
    fun getAllTransfers(): Flow<List<TransferUi>> = transferDao.allTransfersFlow.toTransferUiListFlow(transferDao)

    /**
     * Retrieves a flow of transfers based on the specified transfer direction.
     * Sent transfers are scoped to the selected organization; received transfers include all organizations.
     *
     * @see addTransferByUrl
     * @see addTransferByLinkIdApiV2
     *
     * @param transferDirection The direction of the transfers to retrieve (e.g., [TransferDirection.SENT] or [TransferDirection.RECEIVED]).
     * @return A `Flow` that emits a list of transfers matching the specified direction.
     */
    fun getSortedTransfers(transferDirection: TransferDirection): Flow<SortedTransfers> = userDependentFlow(
        flowForAuthUser = { userId, organizationIdFlow ->
            organizationIdFlow.flatMapLatest { organizationAccountId ->
                val organizationId = organizationAccountId.takeUnless { transferDirection == TransferDirection.RECEIVED }
                combine(
                    transferDao.validTransfersFlow(userId, organizationAccountId = organizationId, transferDirection),
                    transferDao.expiredTransfersFlow(userId, organizationAccountId = organizationId, transferDirection),
                ) { valid, expired ->
                    SortedTransfers(
                        valid.toTransferUiList(transferDao),
                        expired.toTransferUiList(transferDao),
                    )
                }
            }
        },
        flowForGuestUser = {
            combine(
                transferDao.validTransfersFlow(
                    userId = GuestUser.id,
                    organizationAccountId = null,
                    direction = transferDirection,
                ).toTransferUiListFlow(transferDao),
                transferDao.expiredTransfersFlow(
                    userId = GuestUser.id,
                    organizationAccountId = null,
                    direction = transferDirection,
                ).toTransferUiListFlow(transferDao),
            ) { validList, expiredList ->
                SortedTransfers(validList, expiredList)
            }
        },
        merge = { authTransfers, guestTransfers -> authTransfers + guestTransfers }
    ).map {
        // Sort transfers at the end of the flow instead of inside the database to avoid badly sorted transfers when merging valid1 and valid2
        it.sortedByDescendingCreatedDate()
    }

    /**
     * Emits `true` if there's at least one transfer in the database(s), regardless of whether it's in the current account.
     *
     * Use case: Prompting the user to create their first transfer when the emitted value is `false`.
     */
    fun hasAnyTransferFlow(): Flow<Boolean> = transferDao.allTransfersCountFlow.map { it > 0 }.distinctUntilChanged()

    /**
     * Emits 'true' if there is at least one sent transfer for the current account, among all organizations of this account.
     *
     * Unlike [hasAnyTransferFlow], transfers belonging to another account and received transfers are ignored here.
     */
    fun hasAccountTransferFlow(): Flow<Boolean> = userDependentFlow(
        flowForAuthUser = { userId, _ ->
            transferDao.accountTransfersCountFlow(userId, direction = TransferDirection.SENT).map { it > 0 }
        },
        flowForGuestUser = {
            transferDao.accountTransfersCountFlow(userId = GuestUser.id, direction = TransferDirection.SENT).map { it > 0 }
        },
        merge = { hasAuthTransfer, hasGuestTransfer -> hasAuthTransfer || hasGuestTransfer },
    ).distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransferFlow(transferUUID: String): Flow<TransferUi?> = userDependentFlow(
        flowForAuthUser = { userId, _ ->
            transferDao.transferFlow(userId, transferUUID).mapLatest { transferDB -> transferDB?.toTransferUi(transferDao) }
        },
        flowForGuestUser = {
            transferDao.transferFlow(GuestUser.id, transferUUID).mapLatest { it?.toTransferUi(transferDao) }
        },
        merge = { authTransfer, guestTransfer -> authTransfer ?: guestTransfer }
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransferByLinkIdFlow(linkId: String): Flow<TransferUi?> {
        return transferDao.getTransferByLinkIdFlow(linkId).mapLatest { transferDB ->
            transferDB?.toTransferUi(transferDao)
        }
    }

    /**
     * Update all pending transfers in database, most transfers are in [TransferStatus.WAIT_VIRUS_CHECK] status.
     */
    @Throws(CancellationException::class)
    suspend fun fetchWaitingTransfers(): Unit = withContext(Dispatchers.Default) {
        when (val userId = accountManager.currentUser?.id) {
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
        writeDownloadManagerForV2(transferId, fileUid, uniqueDownloadManagerId)
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
        if (accountManager.currentUser == null) error("No user logged in")
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
        return appDatabase.getDownloadManagerRef().getDownloadManagerId(transferId = transfer.uuid, fileId = fileUid ?: "")
    }

    /**
     * Update the local transfer with remote api
     *
     * @throws NotFoundException Any transfer with [transferUUID] has been found
     * @throws NullPropertyException The transferDirection of the transfer found is null
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     *
     */
    @Throws(
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
        val transfer = transferDao.getTransfer(transferId = transferUUID) ?: return
        fetchV2Transfer(transfer)
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
                is ExpiredDateFetchTransferException,
                is NotFoundFetchTransferException -> TransferStatus.EXPIRED_DATE
                is DownloadLimitReached -> TransferStatus.EXPIRED_DOWNLOAD_QUOTA
                else -> throw exception
            }
            transferDao.updateStatus(transferId, newStatus)
        }
    }

    suspend fun updateTransferFilesThumbnails(transferUUID: String, thumbnailRootPath: String) {
        transferDao.getTransfer(transferUUID) ?: return
        appDatabase.useWriterConnection {
            it.immediateTransaction {
                transferDao.getTransferFiles(transferUUID).forEach { file ->
                    val fileDB = file.copy(thumbnailPath = "$thumbnailRootPath/${file.id}")
                    transferDao.upsertFile(fileDB)
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
    @Throws(CancellationException::class)
    suspend fun getTransferByUUID(transferUUID: String): TransferUi? = withContext(Dispatchers.Default) {
        accountManager.currentUser?.id?.let { userId ->
            transferDao.transferFlow(userId, transferUUID).first()?.toTransferUi(transferDao)
        }
    }

    /**
     * Returns the transfer associated with the given link ID as a [TransferUi].
     *
     * Note: This function is only supported for transfers created with API v2.
     *
     * @param linkId the transfer link identifier.
     * @return the matching [TransferUi], or null if no transfer is found.
     */
    suspend fun getTransferByLinkId(linkId: String): TransferUi? = withContext(Dispatchers.Default) {
        return@withContext transferDao.getTransferByLinkId(linkId)?.toTransferUi(transferDao)
    }

    /**
     * Retrieves a transfer by its URL.
     *
     * Analyzes the provided URL to determine whether it corresponds to a v1 or v2 transfer,
     * extracts the relevant identifier, and fetches the transfer from the appropriate source.
     * The result is mapped to [TransferUi]. If the URL format is not supported, an
     * [UnsupportedTransferDeeplinkUrlException] is thrown.
     *
     * @param url The URL of the transfer to retrieve.
     * @return The corresponding [TransferUi] if the transfer is found, or null otherwise.
     */
    @Throws(
        CancellationException::class,
        UnsupportedTransferDeeplinkUrlException::class,
    )
    suspend fun getTransferByUrl(url: String): TransferUi? = withContext(Dispatchers.Default) {
        return@withContext when {
            ApiUrlMatcher.isV1Url(url) -> null
            ApiUrlMatcher.isV2Url(url) -> {
                val linkId = ApiUrlMatcher.extractUUID(url)
                transferDao.getTransferByLinkId(linkId)?.toTransferUi(transferDao)
            }
            else -> throw UnsupportedTransferDeeplinkUrlException(url)
        }
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
     * @return The [TransferUi] of the added transfer, otherwise null if the api doesn't return the transfer
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiErrorException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
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
    suspend fun addTransferByUrl(url: String, password: String? = null): TransferUi? = withContext(Dispatchers.Default) {
        if (!ApiUrlMatcher.isV2Url(url)) return@withContext null
        val linkUUID = extractLinkUUIDFromURL(url) ?: return@withContext null
        val transferApi = transferV2Repository.getTransferByUrl(url, password).transfer
        if (transferDao.getTransfer(transferApi.id) == null) {
            addTransferV2(linkUUID, transferApi, password)
        }
        transferDao.getTransfer(transferApi.id)?.toTransferUi(transferDao)
    }

    private fun extractLinkUUIDFromURL(url: String): String? {
        val maybeLinkUUID = url.substringAfterLast('/')
        return maybeLinkUUID.takeIf { it.isNotEmpty() }
    }

    private suspend fun addTransferV2(linkId: String, transferApi: TransferApiV2, password: String?) {
        val userId = accountManager.currentUser?.id ?: return
        val transferDB = transferApi.toTransferDB(
            direction = TransferDirection.RECEIVED,
            linkId = linkId,
            userOwnerId = userId,
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

    /**
     * Delete a transfer by its UUID.
     *
     * @param transferUUID The UUID of the transfer to be removed.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun deleteTransfer(transferUUID: String): Unit = withContext(Dispatchers.Default) {
        val transfer = transferDao.getTransfer(transferUUID) ?: return@withContext
        transferDao.deleteTransfer(transfer)
    }

    /**
     * Delete Transfers that are expired since a certain amount of days.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun deleteExpiredTransfers() = withContext(Dispatchers.Default) {
        accountManager.currentUser?.let { transferDao.deleteExpiredTransfers() }
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
                    userOwnerId = accountManager.currentUser?.id ?: return
                )
            )
        }
    }

    private suspend fun showGuestData(): Boolean = accountManager.showGuestData.first()

    private fun interface AuthenticatedFlowFactory<T> {
        fun createFlowForAuthUser(userId: Long, organizationIdFlow: Flow<Long?>): Flow<T>
    }

    private fun <T> userDependentFlow(
        flowForAuthUser: AuthenticatedFlowFactory<T>,
        flowForGuestUser: () -> Flow<T>,
        merge: suspend (authUserData: T, guestUserData: T) -> T,
    ): Flow<T> = accountManager.currentUserFlow.flatMapLatest { currentUser ->
        when (currentUser) {
            is STUser.AuthUser -> {
                accountManager.shouldShowGuestData(currentUser).flatMapLatest { shouldShowGuestData ->
                    val dataForAuthenticatedUser = flowForAuthUser.createFlowForAuthUser(
                        userId = currentUser.id,
                        organizationIdFlow = accountManager.selectedOrganizationAccountIdForUser(currentUser.id)
                    )
                    when {
                        shouldShowGuestData -> combine(
                            dataForAuthenticatedUser,
                            flowForGuestUser()
                        ) { authUserData, guestUserData ->
                            merge(authUserData, guestUserData)
                        }
                        else -> dataForAuthenticatedUser
                    }
                }
            }
            GuestUser -> flowForGuestUser()
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
                expiredTransfers = expiredTransfers + other.expiredTransfers,
            )
        }
    }

    private fun SortedTransfers.sortedByDescendingCreatedDate(): SortedTransfers {
        return SortedTransfers(
            validTransfers.sortedByDescending { it.createdDateTimestamp },
            expiredTransfers.sortedByDescending { it.createdDateTimestamp },
        )
    }

    data class DownloadManagerArgs(val transferId: String, val fileId: String?, val uniqueDownloadManagerId: Long?)
}
