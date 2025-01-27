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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.database.controllers.FileController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import com.infomaniak.multiplatform_swisstransfer.network.utils.SharedApiRoutes
import kotlin.coroutines.cancellation.CancellationException

/**
 * Utility class responsible for creating API URLs for shared routes.
 */
class SharedApiUrlCreator internal constructor(
    private val environment: ApiEnvironment,
    private val fileController: FileController,
    private val transferController: TransferController,
    private val uploadController: UploadController,
    private val transferRepository: TransferRepository,
) {
    val createUploadContainerUrl: String = SharedApiRoutes.createUploadContainer(environment)

    fun shareTransferUrl(transferUUID: String) = SharedApiRoutes.shareTransfer(environment, transferUUID)

    @Throws(RealmException::class, CancellationException::class)
    suspend fun downloadFilesUrl(transferUUID: String): String? {
        val transfer = transferController.getTransfer(transferUUID) ?: return null
        val token = transfer.notEmptyPassword?.let { generateToken(transfer, password = it) ?: return null }
        return SharedApiRoutes.downloadFiles(transfer.downloadHost, transfer.linkUUID, token)
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun downloadFileUrl(transferUUID: String, fileUUID: String): String? {
        val transfer = transferController.getTransfer(transferUUID) ?: return null
        val file = fileController.getFile(fileUUID) ?: return null
        val token = transfer.notEmptyPassword?.let { generateToken(transfer, password = it, fileUUID) ?: return null }
        val folderPath = file.path ?: file.fileName
        return when {
            file.isFolder -> SharedApiRoutes.downloadFolder(transfer.downloadHost, transfer.linkUUID, folderPath, token)
            else -> SharedApiRoutes.downloadFile(transfer.downloadHost, transfer.linkUUID, fileUUID, token)
        }
    }

    @Throws(RealmException::class)
    fun uploadChunkUrl(uploadUUID: String, fileUUID: String, chunkIndex: Int, isLastChunk: Boolean): String? {
        val upload = uploadController.getUploadByUUID(uploadUUID) ?: return null
        val containerUUID = upload.remoteContainer?.uuid ?: return null
        val uploadHost = upload.remoteUploadHost ?: return null
        return SharedApiRoutes.uploadChunk(uploadHost, containerUUID, fileUUID, chunkIndex, isLastChunk)
    }

    private val Transfer.notEmptyPassword get() = password?.takeIf { it.isNotEmpty() }

    private suspend fun generateToken(transfer: Transfer, password: String, fileUUID: String? = null): String? = runCatching {
        transferRepository.generateDownloadToken(transfer.containerUUID, fileUUID = fileUUID, password = password)
    }.getOrNull()
}
