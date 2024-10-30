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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class UploadRequest(json: Json, httpClient: HttpClient) : BaseRequest(json, httpClient) {

    suspend fun initUpload(initUploadBody: InitUploadBody): InitUploadResponseApi {
        return post(url = createUrl(ApiRoutes.initUpload), initUploadBody)
    }

    suspend fun verifyEmailCode(verifyEmailCodeBody: VerifyEmailCodeBody): AuthorEmailToken {
        return post(url = createUrl(ApiRoutes.verifyEmailCode), verifyEmailCodeBody)
    }

    suspend fun resendEmailCode(resendEmailCodeBody: ResendEmailCodeBody): Boolean {
        val httpResponse = httpClient.post(url = createUrl(ApiRoutes.resendEmailCode)) {
            setBody(resendEmailCodeBody)
        }
        return httpResponse.status.isSuccess()
    }

    suspend fun uploadChunk(
        uploadHost: String,
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        data: ByteArray,
        onUpload: (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): Boolean {
        val httpResponse = httpClient.post(
            urlString = SharedApiRoutes.uploadChunk(uploadHost, containerUUID, fileUUID, chunkIndex, isLastChunk)
        ) {
            longTimeout()
            setBody(data)
            onUpload { bytesSentTotal, contentLength -> onUpload(bytesSentTotal, contentLength) }
        }
        return httpResponse.status.isSuccess()
    }

    suspend fun finishUpload(finishUploadBody: FinishUploadBody): List<UploadCompleteResponse> {
        return post(createUrl(ApiRoutes.finishUpload), finishUploadBody)
    }
}
