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
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.*
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ContainerErrorsException.Companion.toContainerErrorsException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.EmailValidationException.Companion.toEmailValidationException
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.InitUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ResendEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.VerifyEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.AuthorEmailToken
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.InitUploadResponseApi
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.UploadCompleteResponse
import com.infomaniak.multiplatform_swisstransfer.network.requests.UploadRequest
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

class UploadRepository internal constructor(private val uploadRequest: UploadRequest) {

    // for Obj-C https://youtrack.jetbrains.com/issue/KT-68288/KMP-Support-Kotlin-default-parameters-into-Swift-default-parameters-and-Objective-C-somehow-possibly-a-JvmOverloads-like
    constructor(environment: ApiEnvironment) : this(ApiClientProvider(), environment)
    constructor(apiClientProvider: ApiClientProvider = ApiClientProvider(), environment: ApiEnvironment) : this(
        environment = environment,
        json = apiClientProvider.json,
        httpClient = apiClientProvider.httpClient,
    )

    internal constructor(environment: ApiEnvironment, json: Json, httpClient: HttpClient) : this(
        UploadRequest(environment, json, httpClient)
    )

    @Throws(
        CancellationException::class,
        ContainerErrorsException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun initUpload(
        initUploadBody: InitUploadBody,
        attestationHeaderName: String,
        attestationToken: String,
    ): InitUploadResponseApi {
        return runCatching {
            uploadRequest.initUpload(initUploadBody, attestationHeaderName, attestationToken)
        }.getOrElse { exception ->
            if (exception is UnexpectedApiErrorFormatException) {
                throw exception.toContainerErrorsException()
            } else {
                throw exception
            }
        }
    }

    @Throws(
        CancellationException::class,
        EmailValidationException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun verifyEmailCode(verifyEmailCodeBody: VerifyEmailCodeBody): AuthorEmailToken {
        return runCatching {
            uploadRequest.verifyEmailCode(verifyEmailCodeBody)
        }.getOrElse { exception ->
            if (exception is UnexpectedApiErrorFormatException) {
                throw exception.toEmailValidationException()
            } else {
                throw exception
            }
        }
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun resendEmailCode(resendEmailCodeBody: ResendEmailCodeBody) {
        uploadRequest.resendEmailCode(resendEmailCodeBody)
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun uploadChunk(
        uploadHost: String,
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        isRetry: Boolean,
        data: ByteArray,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ) {
        uploadRequest.uploadChunk(
            uploadHost = uploadHost,
            containerUUID = containerUUID,
            fileUUID = fileUUID,
            chunkIndex = chunkIndex,
            isLastChunk = isLastChunk,
            isRetry = isRetry,
            data = data,
            onUpload = onUpload
        )
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun finishUpload(finishUploadBody: FinishUploadBody): List<UploadCompleteResponse> {
        return uploadRequest.finishUpload(finishUploadBody)
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun cancelUpload(containerUUID: String) {
        val bodyMap = mapOf(
            "UUID" to JsonPrimitive(containerUUID),
            "message" to JsonPrimitive("Cancel by Android User")
        )
        uploadRequest.cancelUpload(JsonObject(bodyMap))
    }
}
