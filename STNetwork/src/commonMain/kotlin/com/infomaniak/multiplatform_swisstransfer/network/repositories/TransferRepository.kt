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
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.Companion.toFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.ExpiredDateFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.NotFoundFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.PasswordNeededFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.VirusCheckFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.VirusDetectedFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException.WrongPasswordFetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.requests.TransferRequest
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

class TransferRepository internal constructor(private val transferRequest: TransferRequest) {

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
        VirusCheckFetchTransferException::class,
        VirusDetectedFetchTransferException::class,
        ExpiredDateFetchTransferException::class,
        NotFoundFetchTransferException::class,
        PasswordNeededFetchTransferException::class,
        WrongPasswordFetchTransferException::class,
    )
    suspend fun getTransferByLinkUUID(linkUUID: String, password: String?): ApiResponse<TransferApi> = runCatching {
        transferRequest.getTransfer(linkUUID, password)
    }.getOrElse { exception ->
        throw if (exception is UnexpectedApiErrorFormatException) exception.toFetchTransferException() else exception
    }

    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        VirusCheckFetchTransferException::class,
        VirusDetectedFetchTransferException::class,
        ExpiredDateFetchTransferException::class,
        NotFoundFetchTransferException::class,
        PasswordNeededFetchTransferException::class,
        WrongPasswordFetchTransferException::class,
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
    )
    suspend fun generateDownloadToken(
        containerUUID: String,
        fileUUID: String?,
        password: String,
    ): String {
        val fileUUIDJson = fileUUID?.let { JsonPrimitive(it) } ?: JsonNull
        val bodyMap = mapOf(
            "containerUUID" to JsonPrimitive(containerUUID),
            "fileUUID" to fileUUIDJson,
            "password" to JsonPrimitive(password),
        )
        return transferRequest.generateDownloadToken(JsonObject(bodyMap))
    }

    @Throws(
        CancellationException::class,
        ApiErrorException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun delete(
        linkUUID: String,
        token: String
    ): Boolean {
        return transferRequest.deleteTransfer(linkUUID, token)
    }


    internal fun extractUUID(url: String) = url.substringAfterLast("/")
}
