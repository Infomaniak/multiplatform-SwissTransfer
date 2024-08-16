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
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.*
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ContainerErrorsException.Companion.toContainerErrorsException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.EmailValidationException.Companion.toEmailValidationException
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.AuthorEmailToken
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.UploadCompleteResponseApi
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.UploadContainerResponseApi
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ContainerRequest
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ResendEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.VerifyEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.requests.UploadRequest
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class UploadRepository internal constructor(private val uploadRequest: UploadRequest) {

    constructor(apiClientProvider: ApiClientProvider = ApiClientProvider()) : this(
        apiClientProvider.json,
        apiClientProvider.httpClient,
    )

    internal constructor(json: Json, httpClient: HttpClient) : this(UploadRequest(json, httpClient))

    @Throws(
        CancellationException::class,
        ContainerErrorsException::class,
        ApiException::class,
        NetworkException::class,
        UnknownApiException::class,
        UnknownException::class,
    )
    suspend fun createContainer(containerRequest: ContainerRequest): UploadContainerResponseApi {
        return runCatching {
            uploadRequest.createContainer(containerRequest)
        }.getOrElse { exception ->
            if (exception is UnknownApiException) {
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
        UnknownApiException::class,
        UnknownException::class,
    )
    suspend fun verifyEmailCode(verifyEmailCodeBody: VerifyEmailCodeBody): AuthorEmailToken {
        return runCatching {
            uploadRequest.verifyEmailCode(verifyEmailCodeBody)
        }.getOrElse { exception ->
            if (exception is UnknownApiException) {
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
        UnknownApiException::class,
        UnknownException::class,
    )
    suspend fun resendEmailCode(resendEmailCodeBody: ResendEmailCodeBody): Boolean {
        return uploadRequest.resendEmailCode(resendEmailCodeBody)
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnknownApiException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun uploadChunk(
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        lastChunk: Boolean,
        data: ByteArray,
    ): Boolean {
        return uploadRequest.uploadChunk(containerUUID, fileUUID, chunkIndex, lastChunk, data)
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        UnknownApiException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun finishUpload(finishUploadBody: FinishUploadBody): List<UploadCompleteResponseApi> {
        return uploadRequest.finishUpload(finishUploadBody)
    }
}
