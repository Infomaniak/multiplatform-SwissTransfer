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
package com.infomaniak.multiplatform_swisstransfer.network.utils

import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment

object SharedApiRoutes {

    fun createUploadContainer(environment: ApiEnvironment): String = "${ApiRoutes.apiBaseUrl(environment)}${ApiRoutes.initUpload}"

    fun shareTransfer(environment: ApiEnvironment, linkUUID: String): String = "${environment.baseUrl}/d/$linkUUID"

    internal fun downloadFilesBase(downloadHost: String, linkUUID: String) = "https://$downloadHost/api/download/$linkUUID"

    fun downloadFiles(downloadHost: String, linkUUID: String, token: String?): String {
        return "${downloadFilesBase(downloadHost, linkUUID)}${withToken(token)}"
    }

    fun downloadFile(downloadHost: String, linkUUID: String, fileUUID: String?, token: String?): String {
        return "${downloadFilesBase(downloadHost, linkUUID)}/$fileUUID${withToken(token)}"
    }

    fun downloadFolder(downloadHost: String, linkUUID: String, folderPath: String, token: String?): String {
        return "${downloadFilesBase(downloadHost, linkUUID)}?folder=$folderPath${withToken(token, prefix = "&")}"
    }

    fun doesChunkExist(
        environment: ApiEnvironment,
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        chunkSize: Long
    ): String {
        val params = "chunk_size=$chunkSize"
        val host = ApiRoutes.apiBaseUrl(environment)
        return "${host}mobile/containers/${containerUUID}/files/${fileUUID}/chunks/${chunkIndex}/exists?$params"
    }

    fun uploadChunk(
        uploadHost: String,
        containerUUID: String,
        fileUUID: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        isRetry: Boolean,
    ): String {
        val retry = if (isRetry) "/retry" else ""
        return "https://$uploadHost/api/uploadChunk/$containerUUID/$fileUUID/$chunkIndex/${isLastChunk.int()}$retry"
    }

    private fun withToken(token: String?, prefix: String = "?") = if (token == null) "" else "${prefix}token=$token"
}
