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
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.network.utils.SharedApiRoutes
import kotlin.coroutines.cancellation.CancellationException

/**
 * Utility class responsible for creating API URLs for shared routes.
 */
class SharedApiUrlCreator internal constructor(
    private val environment: ApiEnvironment,
    private val transferController: TransferController,
    private val uploadController: UploadController,
) {
    val createUploadContainerUrl: String = SharedApiRoutes.createUploadContainer(environment)

    fun shareTransferUrl(transferUUID: String) = SharedApiRoutes.shareTransfer(environment, transferUUID)

    @Throws(RealmException::class, CancellationException::class)
    suspend fun downloadFilesUrl(transferUUID: String): String? {
        val transfer = transferController.getTransfer(transferUUID) ?: return null
        return SharedApiRoutes.downloadFiles(transfer.downloadHost, transfer.linkUUID)
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun downloadFileUrl(transferUUID: String, fileUUID: String?): String? {
        val transfer = transferController.getTransfer(transferUUID) ?: return null
        return SharedApiRoutes.downloadFile(transfer.downloadHost, transfer.linkUUID, fileUUID)
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun downloadFolderUrl(transferUUID: String, folderPath: String): String? {
        val transfer = transferController.getTransfer(transferUUID) ?: return null
        return SharedApiRoutes.downloadFolder(transfer.downloadHost, transfer.linkUUID, folderPath)
    }

    @Throws(RealmException::class)
    fun uploadChunkUrl(uploadUUID: String, fileUUID: String, chunkIndex: Int, isLastChunk: Boolean): String? {
        val upload = uploadController.getUploadByUUID(uploadUUID) ?: return null
        val containerUUID = upload.remoteContainer?.uuid ?: return null
        val uploadHost = upload.remoteUploadHost ?: return null
        return SharedApiRoutes.uploadChunk(uploadHost, containerUUID, fileUUID, chunkIndex, isLastChunk)
    }
}
