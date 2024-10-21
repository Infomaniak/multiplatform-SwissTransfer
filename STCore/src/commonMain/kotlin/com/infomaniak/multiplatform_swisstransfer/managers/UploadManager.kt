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

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.InitUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * Manages upload operations for SwissTransfer.
 *
 * This class handles the initialization, progress, and completion of uploads,
 * interacting with the database and the network repository.
 *
 * @property uploadController The controller for managing upload data in the database.
 * @property uploadRepository The repository for interacting with the SwissTransfer API for uploads.
 */
class UploadManager(
    private val uploadController: UploadController,
    private val uploadRepository: UploadRepository,
) {

    /**
     * Retrieves a list of upload sessions.
     *
     * @return A list of [UploadSession] objects.
     */
    fun getUploads() = uploadController.getUploads()

    /**
     * Initializes an upload session.
     *
     * This method retrieves an upload session from the database using the provided `containerUuid`.
     * If the session is found, it creates an `InitUploadBody` object with the session data and the `recaptcha` token.
     * It then calls the `initUpload()` method of the `uploadRepository` to initiate the upload session on the server.
     * Finally, it updates the upload session in the database with the response received from the server.
     *
     * @param containerUuid The UUID of the upload container.
     * @param recaptcha The reCAPTCHA token.
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun initUploadSession(containerUuid: String, recaptcha: String) {
        uploadController.getUploadByUuid(containerUuid)?.let { uploadSession ->
            val initUploadBody = InitUploadBody(uploadSession, recaptcha)
            val initUploadResponse = uploadRepository.initUpload(initUploadBody)
            uploadController.updateUploadSession(
                uuid = containerUuid,
                remoteContainer = initUploadResponse.container,
                remoteUploadHost = initUploadResponse.uploadHost,
                remoteFilesUuid = initUploadResponse.filesUuid,
            )
        }
    }

    /**
     * Finishes an upload session.
     *
     * This method sends a completion request to the SwissTransfer API
     * and removes the upload session from the database.
     *
     * @param containerUuid The UUID of the upload container.
     */
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

    /**
     * Uploads a chunk of data for a file in an upload session.
     *
     * This method sends a chunk of data to the SwissTransfer API for upload.
     *
     * @param containerUuid The UUID of the upload container.
     * @param fileUuid The UUID of the file being uploaded.
     * @param chunkIndex The index of the chunk being uploaded.
     * @param isLastChunk True if this is the last chunk of the file, false otherwise.
     * @param data The chunk data to upload.
     */
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

    /**
     * Retrieves the total number of uploads in the database.
     *
     * @return The number of uploads.
     */
    fun getUploadsCount() = uploadController.getUploadsCount()
}
