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
package com.infomaniak.multiplatform_swisstransfer.network.repositories

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.requests.TransferRequest
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class TransferRepository internal constructor(private val transferRequest: TransferRequest) {

    // for Obj-C https://youtrack.jetbrains.com/issue/KT-68288/KMP-Support-Kotlin-default-parameters-into-Swift-default-parameters-and-Objective-C-somehow-possibly-a-JvmOverloads-like
    constructor() : this(ApiClientProvider())
    constructor(apiClientProvider: ApiClientProvider = ApiClientProvider()) : this(
        json = apiClientProvider.json,
        httpClient = apiClientProvider.httpClient,
    )

    internal constructor(json: Json, httpClient: HttpClient) : this(TransferRequest(json, httpClient))

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun getTransferByLinkUuid(linkUuid: String): ApiResponse<TransferApi> = transferRequest.getTransfer(linkUuid)

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun getTransferByUrl(url: String): ApiResponse<TransferApi> {
        return transferRequest.getTransfer(extractUuid(url))
    }

    internal fun extractUuid(url: String) = url.substringAfterLast("/")
}
