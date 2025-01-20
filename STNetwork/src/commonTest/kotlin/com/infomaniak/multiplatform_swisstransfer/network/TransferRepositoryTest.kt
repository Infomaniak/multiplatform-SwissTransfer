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
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlin.test.*

class TransferRepositoryTest {

    private val apiClientProvider = ApiClientProvider()
    private val transferRepository = TransferRepository(apiClientProvider, ApiEnvironment.Preprod) // TODO: Use mock client

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
                "linkUUID": "c96350da-4d8c-4c43-aa6a-40f2d2b31eac",
                "containerUUID": "77f4f5c8-6663-42b0-b871-263d3dd11990",
                "downloadCounterCredit": 250,
                "createdDate": "2024-11-26T13:34:49+01:00",
                "expiredDate": "2024-12-26T13:34:49+01:00",
                "isDownloadOnetime": 0,
                "isMailSent": 0,
                "downloadHost": "download.swisstransfer.preprod.dev.infomaniak.ch",
                "container": {
                  "UUID": "77f4f5c8-6663-42b0-b871-263d3dd11990",
                  "duration": 30,
                  "createdDate": "2024-11-26T13:34:49+01:00",
                  "expiredDate": "2024-12-26T13:34:49+01:00",
                  "numberOfFile": 1,
                  "message": "iusfyfuy",
                  "needPassword": 0,
                  "lang": "fr_FR",
                  "sizeUploaded": 26070874,
                  "deletedDate": null,
                  "swiftVersion": 4,
                  "downloadLimit": 250,
                  "source": "ST",
                  "WSUser": null,
                  "files": [
                    {
                      "containerUUID": "77f4f5c8-6663-42b0-b871-263d3dd11990",
                      "UUID": "2e085789-43cb-48f7-ab1f-b7322fbf5367",
                      "fileName": "app-release (1).aab",
                      "fileSizeInBytes": 26070874,
                      "downloadCounter": 0,
                      "createdDate": "2024-11-26T13:34:50+01:00",
                      "expiredDate": "2024-12-26T13:34:50+01:00",
                      "eVirus": "NOT_VIRUS_CHECKED",
                      "deletedDate": null,
                      "mimeType": "",
                      "receivedSizeInBytes": 26070874,
                      "path": null
                    }
                  ]
                }
              }
            }
        """.trimIndent()

        val responseData = apiClientProvider.json.decodeFromString<ApiResponse<TransferApi>>(data).data
        assertNotNull(responseData)

        val file = responseData.container.files.firstOrNull()
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
            apiClientProvider.json.decodeFromString<ApiResponse<TransferApi>>(data)
        }
    }
}
