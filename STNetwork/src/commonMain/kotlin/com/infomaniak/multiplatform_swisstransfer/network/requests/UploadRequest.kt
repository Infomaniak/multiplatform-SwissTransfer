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

import com.infomaniak.multiplatform_swisstransfer.network.models.upload.AuthorEmailToken
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.UploadCompleteResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.UploadContainerResponseApi
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ContainerRequest
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.VerifyEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ResendEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class UploadRequest internal constructor(json: Json, httpClient: HttpClient) : BaseRequest(json, httpClient) {

    suspend fun createContainer(containerRequest: ContainerRequest): UploadContainerResponseApi {
        return post(url = createUrl(ApiRoutes.createContainer), containerRequest)
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
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        lastChunk: Boolean,
        data: ByteArray,
    ): Boolean {
        val httpResponse = httpClient.post(
            url = createUrl(ApiRoutes.uploadChunk(containerUUID, fileUUID, chunkIndex, lastChunk))
        ) {
            setBody(data)
        }
        return httpResponse.status.isSuccess()
    }

    suspend fun finishUpload(finishUploadBody: FinishUploadBody): List<UploadCompleteResponse> {
        return post(createUrl(ApiRoutes.finishUpload), finishUploadBody)
    }
}
