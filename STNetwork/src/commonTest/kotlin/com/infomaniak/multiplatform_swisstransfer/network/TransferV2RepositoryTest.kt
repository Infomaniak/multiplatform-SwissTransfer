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
package com.infomaniak.multiplatform_swisstransfer.network

import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponseV2Success
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.v2.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferV2Repository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TransferV2RepositoryTest {

    private val apiClientProvider = ApiClientProvider()
    private val transferRepository = TransferV2Repository(
        apiClientProvider = apiClientProvider,
        environment = ApiEnvironment.Preprod,
        token = { "dummy_token" }
    ) // TODO: Use mock client

    @Test
    fun canExtractLinkUUIDFromUrl() {
        val url = "https://www.swisstransfer.com/d/fa7d299d-1001-4668-83a4-2a9b61aa59e8"
        val result = transferRepository.extractUUID(url)
        assertEquals("fa7d299d-1001-4668-83a4-2a9b61aa59e8", result)
    }

    @Test
    fun canParseEmptyMimeTypeToNull() {
        val data = """
            {
              "result": "success",
              "data": {
                "id": "c96350da-4d8c-4c43-aa6a-40f2d2b31eac",
                "sender_email": "example@ik.me",
                "title": "Coucou",
                "message": "Voici ce que tu m'as demandé",
                "created_at": 1771324513,
                "expires_at": 1771324513,
                "files": [
                  {
                    "id": "2e085789-43cb-48f7-ab1f-b7322fbf5367",
                    "path": "app-release (1).aab",
                    "size": 26070874,
                    "mimeType": ""
                  }
                ],
                "total_size": 26070874
              }
            }
        """.trimIndent()

        val responseData = apiClientProvider.json.decodeFromString<ApiResponseV2Success<TransferApi>>(data).data
        assertNotNull(responseData)

        val file = responseData.files.firstOrNull()
        assertNotNull(file)

        assertNull(file.mimeType)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun throwWhenApiResponseWithError() {
        val data = """
            {
              "result": "success",
              "data": {
                "type": "need_password",
                "message": "Transfer need a password"
              }
            }
        """.trimIndent()

        assertFailsWith(MissingFieldException::class) {
            apiClientProvider.json.decodeFromString<ApiResponseV2Success<TransferApi>>(data)
        }
    }
}
