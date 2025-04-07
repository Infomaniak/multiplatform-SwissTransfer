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
package com.infomaniak.multiplatform_swisstransfer.network.requests

import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.InitUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ResendEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.VerifyEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.AuthorEmailToken
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.InitUploadResponseApi
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.UploadCompleteResponse
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiRoutes
import com.infomaniak.multiplatform_swisstransfer.network.utils.SharedApiRoutes
import com.infomaniak.multiplatform_swisstransfer.network.utils.longTimeout
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.retry
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class UploadRequest(
    environment: ApiEnvironment,
    json: Json,
    httpClient: HttpClient,
) : BaseRequest(environment, json, httpClient) {

    suspend fun initUpload(
        initUploadBody: InitUploadBody,
        attestationHeaderName: String,
        attestationToken: String,
    ): InitUploadResponseApi {
        val nullableJson = Json(json) {
            explicitNulls = false
        }
        val encodedInitUploadBody = nullableJson.encodeToString(initUploadBody)
        return post(
            url = createUrl(ApiRoutes.initUpload),
            data = encodedInitUploadBody,
            appendHeaders = {
                append(attestationHeaderName, attestationToken)
            },
        )
    }

    suspend fun verifyEmailCode(verifyEmailCodeBody: VerifyEmailCodeBody): AuthorEmailToken {
        return post(url = createUrl(ApiRoutes.verifyEmailCode), verifyEmailCodeBody)
    }

    suspend fun resendEmailCode(resendEmailCodeBody: ResendEmailCodeBody) {
        httpClient.post(url = createUrl(ApiRoutes.resendEmailCode)) {
            contentType(ContentType.Application.Json)
            setBody(resendEmailCodeBody)
        }
    }

    suspend fun doesChunkExist(
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        chunkSize: Long,
    ): Boolean {
        val string = SharedApiRoutes.doesChunkExist(
            environment = environment,
            containerUUID = containerUUID,
            fileUUID = fileUUID,
            chunkIndex = chunkIndex,
            chunkSize = chunkSize
        )
        return get(Url(string))
    }

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
        httpClient.post(
            urlString = SharedApiRoutes.uploadChunk(
                uploadHost = uploadHost,
                containerUUID = containerUUID,
                fileUUID = fileUUID,
                chunkIndex = chunkIndex,
                isLastChunk = isLastChunk, isRetry = isRetry
            )
        ) {
            longTimeout()
            setBody(data)
            onUpload { bytesSentTotal, contentLength -> onUpload(bytesSentTotal, contentLength ?: 0) }
        }
    }

    suspend fun uploadChunk(
        uploadHost: String,
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        data: OutgoingContent.WriteChannelContent,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ) {
        httpClient.post(
            urlString = SharedApiRoutes.uploadChunk(
                uploadHost = uploadHost,
                containerUUID = containerUUID,
                fileUUID = fileUUID,
                chunkIndex = chunkIndex,
                isLastChunk = isLastChunk,
                isRetry = false, // Not needed anymore, including when retrying.
            )
        ) {
            retry { noRetry() }
            longTimeout()
            setBody(data)
            onUpload { bytesSentTotal, contentLength -> onUpload(bytesSentTotal, contentLength ?: 0) }
        }
    }

    suspend fun finishUpload(finishUploadBody: FinishUploadBody): List<UploadCompleteResponse> {
        return post(createUrl(ApiRoutes.finishUpload), finishUploadBody)
    }

    suspend fun cancelUpload(jsonBody: JsonObject) {
        httpClient.post(url = createUrl(ApiRoutes.cancelUpload)) {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
    }
}
