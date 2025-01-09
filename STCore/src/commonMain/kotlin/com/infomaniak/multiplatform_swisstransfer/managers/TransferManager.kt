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
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.DeeplinkException.*
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * TransferManager is responsible for orchestrating data transfer operations
 * using Realm for local data management and an API client for network communications.
 *
 * This class integrates with both RealmProvider and ApiClientProvider to ensure
 * smooth and efficient data transfers, providing a centralized management point
 * for transfer-related activities.
 *
 * @property clientProvider The provider for creating and configuring HTTP clients for API communication.
 * @property transferController The provider for transfer data from database.
 * @property transferRepository The provider for transfer data from api.
 */
class TransferManager internal constructor(
    private val clientProvider: ApiClientProvider,
    private val transferController: TransferController,
    private val transferRepository: TransferRepository,
) {

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
    fun getTransfers(transferDirection: TransferDirection): Flow<List<TransferUi>> {
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
     * Update all pending transfers in database, most transfers are in [TransferStatus.WAIT_VIRUS_CHECK] status.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun fetchWaitingTransfers(): Unit = withContext(Dispatchers.Default) {
        transferController.getNotReadyTransfers().forEach { transfer ->
            runCatching {
                addTransferByLinkUUID(transfer.linkUUID, transfer.password, TransferDirection.SENT)
            }
        }
    }

    /**
     * Designed to keep a reference to the id from Android's DownloadManager.
     */
    suspend fun writeDownloadManagerId(
        transferUUID: String,
        uniqueDownloadManagerId: Long?
    ) {
        transferController.writeDownloadManagerId(transferUUID, uniqueDownloadManagerId)
    }

    /**
     * Gives the id to retrieve a previous download assigned to Android's DownloadManager.
     */
    suspend fun readDownloadManagerId(transferUUID: String): Long? {
        return transferController.readDownloadManagerId(transferUUID)
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
    )
    suspend fun fetchTransfer(transferUUID: String): Unit {
        val localTransfer = transferController.getTransfer(transferUUID)
            ?: throw NotFoundException("No transfer found in DB with uuid = $transferUUID")
        val transferDirection = localTransfer.transferDirection
            ?: throw NullPropertyException("the transferDirection property cannot be null")

        runCatching {
            val remoteTransfer = transferRepository.getTransferByLinkUUID(transferUUID, localTransfer.password).data ?: return
            transferController.upsert(remoteTransfer, transferDirection, localTransfer.password)
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
        return transferController.getTransfer(transferUUID)?.let(::TransferUi)
    }

    /**
     * Retrieves a transfer using the provided link UUID and saves it to the database.
     *
     * This function is typically used after a transfer has been uploaded. Once the upload is complete,
     * a `linkUUID` is returned, which must be passed to this function to retrieve the corresponding transfer.
     * After retrieving the transfer, it is saved to the database.
     *
     * @see getTransfers
     *
     * @param linkUUID The UUID corresponding to the uploaded transfer link or transferUUID.
     * @param password The transfer password if protected
     * @param transferDirection The direction of the transfers to retrieve (e.g., [TransferDirection.SENT])
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     * @throws RealmException An error has occurred with realm database
     * @throws PasswordNeededDeeplinkException If the transfer added via a deeplink is protected by a password
     * @throws WrongPasswordDeeplinkException If we entered a wrong password for a deeplink transfer
     * @throws NotFoundDeeplinkException If the transfer added via a deeplink is expired or doesn't exist
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
        PasswordNeededDeeplinkException::class,
        WrongPasswordDeeplinkException::class,
        NotFoundDeeplinkException::class,
    )
    suspend fun addTransferByLinkUUID(
        linkUUID: String,
        password: String?,
        transferDirection: TransferDirection,
    ): Unit = withContext(Dispatchers.Default) {
        val transferApi = transferRepository.getTransferByLinkUUID(linkUUID, password).data
        addTransfer(transferApi, transferDirection, password)
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
     * @throws ApiException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     * @throws RealmException An error has occurred with realm database
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
    )
    suspend fun addTransferByUrl(url: String, password: String? = null): String? = withContext(Dispatchers.Default) {
        val transferApi = transferRepository.getTransferByUrl(url).data ?: return@withContext null
        addTransfer(transferApi, TransferDirection.RECEIVED, password)
        return@withContext transferApi.linkUUID
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
    suspend fun deleteTransfer(transferUUID: String) = withContext(Dispatchers.Default) {
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

    private suspend fun addTransfer(transferApi: TransferApi?, transferDirection: TransferDirection, password: String?) {
        runCatching {
            transferController.upsert(transferApi as Transfer, transferDirection, password)
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
