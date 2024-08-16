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

internal object ApiRoutes {
    const val baseUrl = "https://www.swisstransfer.com/api/"

    //region Transfer
    const val links = "links"

    fun downloadFiles(downloadHost: String, linkUUID: String): String {
        return "https://$downloadHost/api/download/$linkUUID"
    }

    fun downloadFile(downloadHost: String, linkUUID: String, fileUUID: String?): String {
        return "${downloadFiles(downloadHost, linkUUID)}/$fileUUID"
    }
    //endRegion

    //region Upload
    private const val uploadChunk = "uploadChunk"

    const val initUpload = "containers"
    const val verifyEmailCode = "emails-validation"
    const val resendEmailCode = "$verifyEmailCode/resend"
    const val finishUpload = "uploadComplete"

    fun uploadChunk(containerUUID: String, fileUUID: String, chunkIndex: Int, lastChunk: Boolean): String {
        return "$uploadChunk/$containerUUID/$fileUUID/$chunkIndex/${lastChunk.int()}"
    }
    //endregion

    //region Utils
    private fun Boolean.int() = if (this) 1 else 0
    //endregion
}
