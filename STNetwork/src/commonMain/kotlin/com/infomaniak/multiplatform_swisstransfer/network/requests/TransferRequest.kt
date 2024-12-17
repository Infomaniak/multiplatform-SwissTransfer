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
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal class TransferRequest(
    environment: ApiEnvironment,
    json: Json,
    httpClient: HttpClient,
) : BaseRequest(environment, json, httpClient) {

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getTransfer(linkUUID: String, password: String? = null): ApiResponse<TransferApi> {
        return get(
            url = createUrl(ApiRoutes.getTransfer(linkUUID)),
            appendHeaders = {
                if (password?.isNotEmpty() == true) {
                    append(HttpHeaders.Authorization, Base64.Default.encode(password.encodeToByteArray()))
                }
            }
        )
    }
}
