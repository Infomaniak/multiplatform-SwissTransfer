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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiV2ErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.TooManyRequestException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnauthorizedException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UploadErrorsException.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UploadErrorsException.TransferCancelled
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UploadErrorsException.TransferExpired
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UploadErrorsException.TransferFailed
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.toUploadErrorsException
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.ChunkEtag
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.CreateTransfer
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.UploadTransferStatus
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.v2.PresignedUrlResponse
import com.infomaniak.multiplatform_swisstransfer.network.requests.v2.UploadRequest
import io.ktor.client.HttpClient
import io.ktor.http.content.OutgoingContent
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class UploadV2Repository internal constructor(private val uploadRequest: UploadRequest) {

    // for Obj-C https://youtrack.jetbrains.com/issue/KT-68288/KMP-Support-Kotlin-default-parameters-into-Swift-default-parameters-and-Objective-C-somehow-possibly-a-JvmOverloads-like
    constructor(environment: ApiEnvironment, token: () -> String) : this(ApiClientProvider(), environment, token)
    constructor(
        apiClientProvider: ApiClientProvider = ApiClientProvider(),
        environment: ApiEnvironment,
        token: () -> String,
    ) : this(
        environment = environment,
        json = apiClientProvider.json,
        httpClient = apiClientProvider.httpClient,
        token = token,
    )

    internal constructor(
        environment: ApiEnvironment,
        json: Json,
        httpClient: HttpClient,
        token: () -> String,
    ) : this(UploadRequest(environment, json, httpClient, token))

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        UnauthorizedException::class,
        TooManyRequestException::class,
    )
    suspend fun createTransfer(createTransfer: CreateTransfer): Transfer {
        return withUploadErrorHandling {
            uploadRequest.createTransfer(createTransfer).data
        }
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        NotFoundException::class,
    )
    suspend fun getPresignedUploadUrl(transferId: String, fileId: String): PresignedUrlResponse {
        return withUploadErrorHandling {
            uploadRequest.getPresignedUploadUrl(transferId, fileId).data
        }
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        NotFoundException::class,
    )
    suspend fun getPresignedUploadChunkUrl(
        transferId: String,
        fileId: String,
        chunkIndex: Int,
    ): PresignedUrlResponse {
        return withUploadErrorHandling {
            uploadRequest.getPresignedUploadChunkUrl(transferId, fileId, chunkIndex).data
        }
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        NotFoundException::class,
        TransferCancelled::class,
    )
    suspend fun finalizeTransferFile(transferId: String, fileId: String, etags: List<ChunkEtag>): Boolean {
        return withUploadErrorHandling {
            uploadRequest.finalizeTransferFile(transferId, fileId, etags)
        }
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        UnauthorizedException::class,
        TooManyRequestException::class,
        NotFoundException::class,
        TransferExpired::class,
        TransferCancelled::class,
    )
    suspend fun finalizeTransfer(transferId: String): Boolean {
        return withUploadErrorHandling {
            uploadRequest.updateTransferStatus(transferId, UploadTransferStatus.Completed)
        }
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        UnauthorizedException::class,
        TooManyRequestException::class,
        NotFoundException::class,
        TransferExpired::class,
        TransferCancelled::class,
        TransferFailed::class,
    )
    suspend fun cancelTransfer(transferId: String): Boolean {
        return withUploadErrorHandling {
            uploadRequest.updateTransferStatus(transferId, UploadTransferStatus.Cancelled)
        }
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        UnauthorizedException::class,
        TooManyRequestException::class,
        NotFoundException::class,
        TransferExpired::class,
        TransferCancelled::class,
        TransferFailed::class,
    )
    suspend fun markTransferAsFailed(transferId: String): Boolean {
        return withUploadErrorHandling {
            uploadRequest.updateTransferStatus(transferId, UploadTransferStatus.Failed)
        }
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun uploadFile(
        cephUrl: String,
        data: OutgoingContent.WriteChannelContent,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ) = withUploadErrorHandling {
        uploadRequest.uploadFile(cephUrl, data, onUpload)
    }

    @Throws(
        CancellationException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun uploadChunkToEtag(
        cephUrl: String,
        data: OutgoingContent.WriteChannelContent,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): String? = withUploadErrorHandling {
        uploadRequest.uploadChunk(cephUrl, data, onUpload)
    }

    private suspend fun <T> withUploadErrorHandling(block: suspend () -> T) = runCatching {
        block()
    }.getOrElse { exception ->
        throw when (exception) {
            is ApiV2ErrorException if exception.code == "not_authorized" -> UnauthorizedException(exception.requestContextId)
            is ApiV2ErrorException if exception.code == "too_many_request" -> TooManyRequestException(exception.requestContextId)
            is ApiV2ErrorException -> exception.toUploadErrorsException()
            else -> exception
        }
    }
}
