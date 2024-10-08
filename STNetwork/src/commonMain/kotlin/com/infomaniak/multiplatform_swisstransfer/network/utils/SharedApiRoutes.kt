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

object SharedApiRoutes {

    fun downloadFiles(downloadHost: String, linkUuid: String): String = "https://$downloadHost/api/download/$linkUuid"

    fun downloadFile(downloadHost: String, linkUuid: String, fileUuid: String?): String {
        return "${downloadFiles(downloadHost, linkUuid)}/$fileUuid"
    }

    fun uploadChunk(uploadHost: String, containerUuid: String, fileUuid: String, chunkIndex: Int, isLastChunk: Boolean): String {
        return "https://$uploadHost/api/uploadChunk/$containerUuid/$fileUuid/$chunkIndex/${isLastChunk.int()}"
    }
}
