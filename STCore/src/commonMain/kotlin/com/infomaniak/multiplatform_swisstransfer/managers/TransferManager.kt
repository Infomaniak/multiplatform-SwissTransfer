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

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.TransferController
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
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
     * @see addTransferByLinkUuid
     *
     * @param transferDirection The direction of the transfers to retrieve (e.g., [TransferDirection.SENT] or [TransferDirection.RECEIVED]).
     * @return A `Flow` that emits a list of transfers matching the specified direction.
     */
    fun getTransfers(transferDirection: TransferDirection): Flow<List<Transfer>> {
        return transferController.getTransfersFlow(transferDirection).flowOn(Dispatchers.IO)
    }

    /**
     * Retrieves a transfer using the provided link UUID and saves it to the database.
     *
     * This function is typically used after a transfer has been uploaded. Once the upload is complete,
     * a `linkUuid` is returned, which must be passed to this function to retrieve the corresponding transfer.
     * After retrieving the transfer, it is saved to the database.
     *
     * @see getTransfers
     *
     * @param linkUuid The UUID corresponding to the uploaded transfer link.
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun addTransferByLinkUuid(linkUuid: String) = withContext(Dispatchers.IO) {
        addTransfer(transferRepository.getTransferByLinkUuid(linkUuid).data, TransferDirection.SENT)
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
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is an error related to the API during transfer retrieval.
     * @throws UnexpectedApiErrorFormatException Unparsable api error response.
     * @throws NetworkException If there is a network issue during the transfer retrieval.
     * @throws UnknownException Any error not already handled by the above ones.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun addTransferByUrl(url: String) = withContext(Dispatchers.IO) {
        addTransfer(transferRepository.getTransferByUrl(url).data, TransferDirection.RECEIVED)
    }

    private suspend fun addTransfer(transferApi: TransferApi?, transferDirection: TransferDirection) {
        runCatching {
            transferController.upsert(transferApi as Transfer, transferDirection)
        }.onFailure {
            throw UnknownException(it)
        }
    }
}
