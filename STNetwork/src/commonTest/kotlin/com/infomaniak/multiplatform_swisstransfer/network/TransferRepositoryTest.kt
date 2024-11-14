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

import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponse
import com.infomaniak.multiplatform_swisstransfer.network.models.transfer.TransferApi
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransferRepositoryTest {

    private val apiClientProvider = ApiClientProvider()
    private val transferRepository = TransferRepository(apiClientProvider) // TODO: Use mock client

    @Test
    fun canExtractLinkUUIDFromUrl() {
        val url = "https://www.swisstransfer.com/d/fa7d299d-1001-4668-83a4-2a9b61aa59e8"
        val result = transferRepository.extractUUID(url)
        assertEquals("fa7d299d-1001-4668-83a4-2a9b61aa59e8", result)
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
