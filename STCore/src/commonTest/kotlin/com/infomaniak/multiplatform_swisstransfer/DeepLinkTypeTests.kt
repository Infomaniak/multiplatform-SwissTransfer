/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer

import com.infomaniak.multiplatform_swisstransfer.data.DeepLinkType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class DeepLinkTypeTests {
    // General tests

    @Test
    fun emptyURLTest() {
        assertNull(DeepLinkType.fromURL(""))
    }

    @Test
    fun notHandledURLTest() {
        assertNull(DeepLinkType.fromURL("https://somedomain.com/something/else"))
    }

    // DeleteTransfer

    @Test
    fun deleteTransfer_correctURLTest() {
        possibleURLs.forEach { url ->
            assertIs<DeepLinkType.DeleteTransfer>(DeepLinkType.fromURL("$url/d/1234-5678-9012?delete=1234-5678-9012"))
        }
    }

    @Test
    fun deleteTransfer_extractedValuesTest() {
        val token = FAKE_UUID.reversed()

        val deleteTransfer = DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/d/$FAKE_UUID?delete=$token")
        assertIs<DeepLinkType.DeleteTransfer>(deleteTransfer)

        if (deleteTransfer is DeepLinkType.DeleteTransfer) {
            assertEquals(FAKE_UUID, deleteTransfer.uuid)
            assertEquals(token, deleteTransfer.token)
        }
    }

    @Test
    fun deleteTransfer_missingTokenTest() {
        assertNull(DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/d/1234-5678-9012?delete="))
    }

    @Test
    fun deleteTransfer_incorrectURLTest() {
        assertNull(DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/1234-5678-9012?delete=1234-5678-9012"))
    }

    // OpenTransfer

    @Test
    fun openTransfer_correctURLTest() {
        possibleURLs.forEach { url ->
            assertIs<DeepLinkType.OpenTransfer>(DeepLinkType.fromURL("$url/d/1234-5678-9012"))
        }
    }

    @Test
    fun openTransfer_missingUUIDTest() {
        assertNull(DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/d/"))
    }

    @Test
    fun openTransfer_incorrectURLTest() {
        assertNull(DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/1234-5678-9012"))
    }

    @Test
    fun openTransfer_extractedValuesTest() {
        val openTransfer = DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/d/$FAKE_UUID")
        assertIs<DeepLinkType.OpenTransfer>(openTransfer)

        if (openTransfer is DeepLinkType.OpenTransfer) {
            assertEquals(FAKE_UUID, openTransfer.uuid)
        }
    }

    // ImportTransferFromExtension

    @Test
    fun importTransferFromExtension_correctURLTest() {
        possibleURLs.forEach { url ->
            assertIs<DeepLinkType.ImportTransferFromExtension>(DeepLinkType.fromURL("$url/import?uuid=1234-5678-9012"))
        }
    }

    @Test
    fun importTransferFromExtension_extractedValuesTest() {
        val importTransfer = DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/import?uuid=$FAKE_UUID")
        assertIs<DeepLinkType.ImportTransferFromExtension>(importTransfer)

        if (importTransfer is DeepLinkType.ImportTransferFromExtension) {
            assertEquals(FAKE_UUID, importTransfer.uuid)
        }
    }

    @Test
    fun importTransferFromExtension_missingUUIDTest() {
        assertNull(DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/import"))
    }

    @Test
    fun importTransferFromExtension_missingUUIDValueTest() {
        assertNull(DeepLinkType.fromURL("$SWISSTRANSFER_PROD_URL/import?uuid="))
    }

    companion object {
        private const val SWISSTRANSFER_PROD_URL = "https://www.swisstransfer.com"
        private const val SWISSTRANSFER_PREPROD_URL = "https://swisstransfer.preprod.dev.infomaniak.ch"
        private const val SWISSTRANSFER_RANDOM_URL = "https://custom_st_url.ch"

        private val possibleURLs = arrayOf(SWISSTRANSFER_PROD_URL, SWISSTRANSFER_PREPROD_URL, SWISSTRANSFER_RANDOM_URL)

        private val FAKE_UUID = "1234-5678-9012"
    }
}
