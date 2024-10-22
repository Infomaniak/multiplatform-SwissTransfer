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
import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.data.NewUploadSession
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ContainerErrorsException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.InitUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
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
     * Retrieves all upload sessions from the database.
     *
     * @return A list of all upload sessions.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getUploads(): List<UploadSession> = withContext(Dispatchers.IO) {
        return@withContext uploadController.getUploads()
    }

    /**
     * Retrieves the total number of uploads in the database.
     *
     * @return The number of uploads.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getUploadsCount(): Long = withContext(Dispatchers.IO) {
        return@withContext uploadController.getUploadsCount()
    }

    /**
     * Creates a new upload session in the database.
     *
     * This method inserts a new upload session into the database using the provided `newUploadSession` data.
     *
     * @param newUploadSession The data for the new upload session.
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun createUpload(newUploadSession: NewUploadSession) = withContext(Dispatchers.IO) {
        uploadController.insert(newUploadSession)
    }

    /**
     * Initializes an upload session.
     *
     * This method retrieves an upload session from the database using the provided `uuid`.
     * If the session is found, it creates an `InitUploadBody` object with the session data and the `recaptcha` token.
     * It then calls the `initUpload()` method of the `uploadRepository` to initiate the upload session on the server.
     * Finally, it updates the upload session in the database with the response received from the server.
     *
     * @param uuid The UUID of the upload session.
     * @param recaptcha The reCAPTCHA token or an empty string in any.
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     * @throws ContainerErrorsException If there is an error with the container.
     * @throws ApiException If there is a general API error.
     * @throws NetworkException If there is a network error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     */
    @Throws(
        RealmException::class,
        CancellationException::class,
        ContainerErrorsException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun initUploadSession(uuid: String, recaptcha: String = ""): Unit = withContext(Dispatchers.IO) {
        uploadController.getUploadByUUID(uuid)?.let { uploadSession ->
            val initUploadBody = InitUploadBody(uploadSession, recaptcha)
            val initUploadResponse = uploadRepository.initUpload(initUploadBody)
            uploadController.updateUploadSession(
                uuid = uuid,
                remoteContainer = initUploadResponse.container,
                remoteUploadHost = initUploadResponse.uploadHost,
                remoteFilesUUID = initUploadResponse.filesUUID,
            )
        }
    }

    /**
     * Uploads a chunk of data for a file in an upload session.
     *
     * This method retrieves an upload session from the database using the provided `uuid`.
     * If the session is found and has a remote upload host and remote container, it calls the `uploadChunk()` method of the
     * `uploadRepository` to send the chunk data to the server.
     *
     * @param uuid The UUID of the upload session.
     * @param fileUUID The UUID of the file being uploaded.
     * @param chunkIndex The index of the chunk being uploaded.
     * @param isLastChunk True if this is the last chunk of the file, false otherwise.
     * @param data The chunk data to upload.
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws NetworkException If there is a network error.
     * @throws UnknownException If an unknown error occurs.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
    )
    suspend fun uploadChunk(
        uuid: String,
        fileUUID: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        data: ByteArray,
    ): Unit = withContext(Dispatchers.IO) {
        val uploadSession = uploadController.getUploadByUUID(uuid) ?: return@withContext
        val remoteUploadHost = uploadSession.remoteUploadHost ?: return@withContext
        val remoteContainer = uploadSession.remoteContainer ?: return@withContext

        uploadRepository.uploadChunk(
            uploadHost = remoteUploadHost,
            containerUUID = remoteContainer.uuid,
            fileUUID = fileUUID,
            chunkIndex = chunkIndex,
            isLastChunk = isLastChunk,
            data = data,
        )
    }

    /**
     * Finishes an upload session.
     *
     * This method retrieves an upload session from the database using the provided `uuid`.
     * If the session is found and has a remote container UUID, it creates a `FinishUploadBody` object
     * with the necessary data and calls the `finishUpload()` method of the `uploadRepository` to
     * finalize the upload session on the server.
     * Finally, it removes the upload session from the database.
     *
     * @param uuid The UUID of the upload session.
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws NetworkException If there is a network error.
     * @throws UnknownException If an unknown error occurs.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
    )
    suspend fun finishUploadSession(uuid: String): Unit = withContext(Dispatchers.IO) {
        val uploadSession = uploadController.getUploadByUUID(uuid) ?: return@withContext
        val containerUUID = uploadSession.remoteContainer?.uuid ?: return@withContext

        val finishUploadBody = FinishUploadBody(
            containerUUID = containerUUID,
            language = uploadSession.language.code,
            recipientsEmails = uploadSession.recipientsEmails,
        )
        uploadRepository.finishUpload(finishUploadBody)
        uploadController.removeUploadSession(containerUUID)
    }
}
