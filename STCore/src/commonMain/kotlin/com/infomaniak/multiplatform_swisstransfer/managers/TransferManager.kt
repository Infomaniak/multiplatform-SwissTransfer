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
import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.TransferController
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnknownApiException
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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

    val transfers get() = transferController.getTransfersFlow().flowOn(Dispatchers.IO)

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnknownApiException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun addTransferByLinkUuid(linkUuid: String) = withContext(Dispatchers.IO) {
        addTransfer(transferRepository.getTransferByLinkUuid(linkUuid).data)
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnknownApiException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun addTransferByUrl(url: String) = withContext(Dispatchers.IO) {
        addTransfer(transferRepository.getTransferByUrl(url).data)
    }

    private suspend fun addTransfer(transferApi: TransferApi?) {
        runCatching {
            transferController.upsert(transferApi as Transfer<*>)
        }.onFailure {
            throw UnknownException(it)
        }
    }
}
