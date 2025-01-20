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
package com.infomaniak.multiplatform_swisstransfer.network

import com.infomaniak.multiplatform_swisstransfer.network.utils.SharedApiRoutes
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedApiRoutesTest {

    @Test
    fun downloadFilesBase_IsCorrect() {
        val result = SharedApiRoutes.downloadFilesBase("host", "linkUUID")
        assertEquals("https://host/api/download/linkUUID", result)
    }

    @Test
    fun downloadFilesUrl_IsCorrect() {
        val result = SharedApiRoutes.downloadFiles("host", "linkUUID", null)
        assertEquals(SharedApiRoutes.downloadFilesBase("host", "linkUUID"), result)
    }

    @Test
    fun downloadFilesUrlWithPassword_IsCorrect() {
        val result = SharedApiRoutes.downloadFiles("host", "linkUUID", "token")
        val url = SharedApiRoutes.downloadFilesBase("host", "linkUUID") + "?token=token"
        assertEquals(url, result)
    }

    @Test
    fun downloadFileUrl_IsCorrect() {
        val result = SharedApiRoutes.downloadFile("host", "linkUUID", "fileUUID", null)
        val expected = SharedApiRoutes.downloadFilesBase("host", "linkUUID") + "/fileUUID"
        assertEquals(expected, result)
    }

    @Test
    fun downloadFileUrlWithPassword_IsCorrect() {
        val result = SharedApiRoutes.downloadFile("host", "linkUUID", "fileUUID", "token")
        val expected = SharedApiRoutes.downloadFilesBase("host", "linkUUID") + "/fileUUID?token=token"
        assertEquals(expected, result)
    }

    @Test
    fun downloadFolderUrl_IsCorrect() {
        val result = SharedApiRoutes.downloadFolder("host", "linkUUID", "folderPath", null)
        val expected = SharedApiRoutes.downloadFilesBase("host", "linkUUID") + "?folder=folderPath"
        assertEquals(expected, result)
    }

    @Test
    fun downloadFolderUrlWithPassword_IsCorrect() {
        val result = SharedApiRoutes.downloadFolder("host", "linkUUID", "folderPath", "token")
        val expected = SharedApiRoutes.downloadFilesBase("host", "linkUUID") + "?folder=folderPath&token=token"
        assertEquals(expected, result)
    }
}
