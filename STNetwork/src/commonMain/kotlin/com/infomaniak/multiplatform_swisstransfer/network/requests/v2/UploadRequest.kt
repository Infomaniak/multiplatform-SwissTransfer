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
package com.infomaniak.multiplatform_swisstransfer.network.requests.v2

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.ChunkEtag
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.CreateTransfer
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.UploadTransferStatus
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.v2.PresignedUrlResponse
import com.infomaniak.multiplatform_swisstransfer.network.requests.BaseRequest
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiRoutes
import com.infomaniak.multiplatform_swisstransfer.network.utils.longTimeout
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.retry
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class UploadRequest(
    environment: ApiEnvironment,
    json: Json,
    httpClient: HttpClient,
    token: () -> String,
) : BaseRequest(environment, json, httpClient, token) {

    suspend fun createTransfer(createTransfer: CreateTransfer): ApiResponse<Transfer> {
        val nullableJson = Json(json) {
            explicitNulls = false
        }
        return post(
            url = createV2Url(ApiRoutes.createTransfer()),
            data = nullableJson.encodeToString(createTransfer),
            appendHeaders = { appendBearer() }
        )
    }

    suspend fun getPresignedUploadUrl(transferId: String, fileId: String): ApiResponse<PresignedUrlResponse> {
        return post(
            url = createV2Url(ApiRoutes.uploadDirectly(transferId, fileId)),
            data = null,
        )
    }

    suspend fun getPresignedUploadChunkUrl(
        transferId: String,
        fileId: String,
        chunkIndex: Int,
    ): ApiResponse<PresignedUrlResponse> {
        return post(
            url = createV2Url(ApiRoutes.uploadChunk(transferId, fileId, chunkIndex)),
            data = null,
        )
    }

    suspend fun finalizeTransferFile(transferId: String, fileId: String, etags: List<ChunkEtag>): Boolean {
        val response = httpClient.patch(createV2Url(ApiRoutes.uploadDirectly(transferId, fileId))) {
            contentType(ContentType.Application.Json)
            setBody(etags)
        }
        return response.status.isSuccess()
    }

    suspend fun updateTransferStatus(transferId: String, status: UploadTransferStatus): Boolean {
        val response = httpClient.patch(createV2Url(ApiRoutes.finishTransfer(transferId))) {
            headers { appendBearer() }
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status.apiValue))
        }
        return response.status.isSuccess()
    }

    suspend fun uploadFile(
        cephUrl: String,
        data: OutgoingContent.WriteChannelContent,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ) {
        httpClient.post(urlString = cephUrl) {
            retry { noRetry() }
            longTimeout()
            setBody(data)
            onUpload { bytesSentTotal, contentLength -> onUpload(bytesSentTotal, contentLength ?: 0) }
        }
    }

    suspend fun uploadChunk(
        cephUrl: String,
        data: ByteArray,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): String? {
        val response = httpClient.post(urlString = cephUrl) {
            longTimeout()
            setBody(data)
            onUpload { bytesSentTotal, contentLength -> onUpload(bytesSentTotal, contentLength ?: 0) }
        }
        return if (response.status.isSuccess()) response.headers["Etag"] else null
    }

    suspend fun uploadChunk(
        cephUrl: String,
        data: OutgoingContent.WriteChannelContent,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): String? {
        val response = httpClient.post(urlString = cephUrl) {
            retry { noRetry() }
            longTimeout()
            setBody(data)
            onUpload { bytesSentTotal, contentLength -> onUpload(bytesSentTotal, contentLength ?: 0) }
        }

        return if (response.status.isSuccess()) response.headers["Etag"] else null
    }
}
