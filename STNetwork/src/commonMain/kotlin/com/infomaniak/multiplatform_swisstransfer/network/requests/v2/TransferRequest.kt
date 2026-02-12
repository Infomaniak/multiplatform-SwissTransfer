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

import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponseV2Success
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.v2.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.v2.PresignedUrlResponse
import com.infomaniak.multiplatform_swisstransfer.network.requests.BaseRequest
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.http.HeadersBuilder
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlin.io.encoding.ExperimentalEncodingApi

internal class TransferRequest(
    environment: ApiEnvironment,
    json: Json,
    httpClient: HttpClient,
    token: () -> String,
) : BaseRequest(environment, json, httpClient, token) {

    suspend fun getTransfer(linkUUID: String, password: String? = null): ApiResponseV2Success<TransferApi> {
        return get(
            url = createV2Url(ApiRoutes.getTransfer(linkUUID)),
            appendHeaders = {
                appendBearer()
                appendPasswordIfNeeded(password)
            }
        )
    }

    suspend fun presignedDownloadUrl(
        linkId: String,
        fileId: String,
        password: String?
    ): ApiResponseV2Success<PresignedUrlResponse> = get(
        url = createV2Url(ApiRoutes.presignedDownloadUrl(linkId, fileId)),
        appendHeaders = { appendPasswordIfNeeded(password) }
    )

    suspend fun deleteTransfer(transferId: String): Boolean {
        val response = httpClient.delete(
            url = createV2Url(ApiRoutes.v2DeleteTransfer(transferId)),
        )
        return response.status.isSuccess()
    }

    private fun HeadersBuilder.appendPasswordIfNeeded(password: String?) {
        if (password?.isNotEmpty() == true) {
            append("Transfer-Password", password)
        }
    }
}
