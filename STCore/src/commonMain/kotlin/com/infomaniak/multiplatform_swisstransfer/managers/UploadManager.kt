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
package com.infomaniak.multiplatform_swisstransfer.managers

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.InitUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository

class UploadManager(
    private val uploadController: UploadController,
    private val uploadRepository: UploadRepository,
) {

    fun getUploads(hasBeenInitialized: Boolean?): List<UploadSession> {
        return hasBeenInitialized?.let {
            uploadController.getUploads(hasBeenInitialized)
        } ?: uploadController.getAllUploads()
    }

    suspend fun initUploadSession(containerUuid: String, recaptcha: String) {
        uploadController.getUploadByUuid(containerUuid)?.let { uploadSession ->
            val initUploadBody = InitUploadBody(uploadSession, recaptcha)
            val initUploadResponse = uploadRepository.initUpload(initUploadBody)
            runCatching {
                uploadController.updateUploadSession(
                    uuid = containerUuid,
                    remoteContainer = initUploadResponse.container,
                    remoteUploadHost = initUploadResponse.uploadHost,
                    remoteFilesUuid = initUploadResponse.filesUuid,
                )
            }
        }
    }

    suspend fun finishUploadSession(containerUuid: String) {
        uploadController.getUploadByUuid(containerUuid)?.let { uploadSession ->
            val finishUploadBody = FinishUploadBody(
                containerUuid = containerUuid,
                language = uploadSession.language,
                recipientsEmails = uploadSession.recipientsEmails,
            )
            uploadRepository.finishUpload(finishUploadBody)
            uploadController.removeUploadSession(containerUuid)
        }
    }

    suspend fun uploadChunk(
        containerUuid: String,
        fileUuid: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        data: ByteArray,
    ) {
        uploadController.getUploadByUuid(containerUuid)?.let { uploadSession ->
            if (uploadSession.remoteUploadHost == null) return@let

            uploadRepository.uploadChunk(
                uploadHost = uploadSession.remoteUploadHost!!,
                containerUuid = containerUuid,
                fileUuid = fileUuid,
                chunkIndex = chunkIndex,
                isLastChunk = isLastChunk,
                data = data,
            )
        }
    }
}
