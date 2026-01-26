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
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiV2ErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.Companion.toFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.DownloadLimitReached
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.ExpiredDateFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.NotFoundFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.PasswordNeededFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.TransferCancelledException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.v2.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.requests.v2.TransferRequest
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class TransferV2Repository internal constructor(private val transferRequest: TransferRequest) {

    // for Obj-C https://youtrack.jetbrains.com/issue/KT-68288/KMP-Support-Kotlin-default-parameters-into-Swift-default-parameters-and-Objective-C-somehow-possibly-a-JvmOverloads-like
    constructor(environment: ApiEnvironment) : this(ApiClientProvider(), environment)
    constructor(apiClientProvider: ApiClientProvider = ApiClientProvider(), environment: ApiEnvironment) : this(
        environment = environment,
        json = apiClientProvider.json,
        httpClient = apiClientProvider.httpClient,
    )

    internal constructor(environment: ApiEnvironment, json: Json, httpClient: HttpClient) :
            this(TransferRequest(environment, json, httpClient))

    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        PasswordNeededFetchTransferException::class,
        NotFoundFetchTransferException::class,
        TransferCancelledException::class,
        ExpiredDateFetchTransferException::class,
        DownloadLimitReached::class,
    )
    suspend fun getTransferByLinkUUID(linkUUID: String, password: String?): ApiResponse<TransferApi> = withTransferErrorHandling {
        transferRequest.getTransfer(linkUUID, password)
    }

    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        PasswordNeededFetchTransferException::class,
        NotFoundFetchTransferException::class,
        TransferCancelledException::class,
        ExpiredDateFetchTransferException::class,
        DownloadLimitReached::class,
    )
    suspend fun getTransferByUrl(url: String, password: String?): ApiResponse<TransferApi> {
        return getTransferByLinkUUID(extractUUID(url), password)
    }

    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        PasswordNeededFetchTransferException::class,
        NotFoundFetchTransferException::class,
        TransferCancelledException::class,
        ExpiredDateFetchTransferException::class,
        DownloadLimitReached::class,
    )
    suspend fun presignedDownloadUrl(linkId: String, fileId: String, password: String?) = withTransferErrorHandling {
        transferRequest.presignedDownloadUrl(linkId, fileId, password)
    }

    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        PasswordNeededFetchTransferException::class,
        NotFoundFetchTransferException::class,
        TransferCancelledException::class,
        ExpiredDateFetchTransferException::class,
    )
    suspend fun delete(
        transferId: String,
    ): Boolean = withTransferErrorHandling {
        transferRequest.deleteTransfer(transferId)
    }


    internal fun extractUUID(url: String) = url.substringAfterLast("/")

    private suspend fun <T> withTransferErrorHandling(block: suspend () -> T) = runCatching {
        block()
    }.getOrElse { exception ->
        throw when (exception) {
            is ApiV2ErrorException -> exception.toFetchTransferException()
            else -> exception
        }
    }
}
