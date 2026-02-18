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
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal class TransferRequest(
    environment: ApiEnvironment,
    json: Json,
    httpClient: HttpClient,
) : BaseRequest(environment, json, httpClient, token = { "" }) {

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getTransfer(linkUUID: String, password: String? = null): ApiResponse<TransferApi> {
        return get(
            url = createUrl(ApiRoutes.getTransfer(linkUUID)),
            appendHeaders = {
                if (password?.isNotEmpty() == true) {
                    append(HttpHeaders.Authorization, Base64.encode(password.encodeToByteArray()))
                }
            }
        )
    }

    suspend fun deleteTransfer(linkUUID: String, token: String): Boolean {
        val payload = buildJsonObject {
            put("linkUUID", linkUUID)
            put("token", token)
        }

        val response = httpClient.post(
            url = createUrl(ApiRoutes.disableLinks())
        ) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    suspend fun generateDownloadToken(jsonBody: JsonObject): String {
        val httpResponse = httpClient.post(url = createUrl(ApiRoutes.generateDownloadToken())) {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        return httpResponse.bodyAsText().trim('"')
    }
}
