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
package com.infomaniak.multiplatform_swisstransfer

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.network.utils.SharedApiRoutes

/**
 * Utility class responsible for creating API URLs for shared routes.
 */
class SharedApiUrlCreator internal constructor(
    private val transferController: TransferController,
    private val uploadController: UploadController,
) {

    @Throws(RealmException::class)
    fun downloadFilesUrl(transferUuid: String): String? {
        val transfer = transferController.getTransfer(transferUuid) ?: return null
        return SharedApiRoutes.downloadFiles(transfer.downloadHost, transfer.linkUuid)
    }

    @Throws(RealmException::class)
    fun downloadFileUrl(transferUuid: String, fileUuid: String?): String? {
        val transfer = transferController.getTransfer(transferUuid) ?: return null
        return SharedApiRoutes.downloadFile(transfer.downloadHost, transfer.linkUuid, fileUuid)
    }

    @Throws(RealmException::class)
    fun uploadChunkUrl(uploadUuid: String, fileUuid: String, chunkIndex: Int, isLastChunk: Boolean): String? {
        val upload = uploadController.getUploadByUuid(uploadUuid) ?: return null
        val containerUuid = upload.container?.uuid ?: return null
        return SharedApiRoutes.uploadChunk(upload.uploadHost, containerUuid, fileUuid, chunkIndex, isLastChunk)
    }
}
