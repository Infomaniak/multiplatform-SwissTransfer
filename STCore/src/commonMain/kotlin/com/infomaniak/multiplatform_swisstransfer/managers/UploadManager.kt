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
import com.infomaniak.multiplatform_swisstransfer.exceptions.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.exceptions.NullPropertyException
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
 * @property transferManager Transfer operations
 */
class UploadManager(
    private val uploadController: UploadController,
    private val uploadRepository: UploadRepository,
    private val transferManager: TransferManager,
) {

    /**
     * Retrieves all upload sessions from the database.
     *
     * @return A list of all upload sessions.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getUploads(): List<UploadSession> = withContext(Dispatchers.IO) {
        return@withContext uploadController.getAllUploads()
    }

    /**
     * Retrieves the last upload session from the database.
     *
     * @return Last upload session.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getLastUpload(): UploadSession? = withContext(Dispatchers.IO) {
        return@withContext uploadController.getLastUpload()
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
     * @param newUploadSession The data for the new upload session.
     *
     * @return The inserted [UploadSession]
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun createAnGetUpload(newUploadSession: NewUploadSession): UploadSession = withContext(Dispatchers.IO) {
        uploadController.insertAndGet(newUploadSession)
    }

    /**
     * Initializes an upload session and update it in database with the remote data.
     *
     * @param uuid The UUID of the upload session or null to use the last upload session.
     * @param recaptcha The reCAPTCHA token or an empty string in any.
     *
     * @return The upload session that has been updated or null if it no longer exists after the update.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     * @throws ContainerErrorsException If there is an error with the container.
     * @throws ApiException If there is a general API error.
     * @throws NetworkException If there is a network error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws NotFoundException If we cannot find the upload session in the database with the specified uuid.
     */
    @Throws(
        RealmException::class,
        CancellationException::class,
        ContainerErrorsException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        NotFoundException::class,
    )
    suspend fun initUploadSession(uuid: String? = null, recaptcha: String = ""): UploadSession? = withContext(Dispatchers.IO) {

        val uploadSession = when (uuid) {
            null -> uploadController.getLastUpload() ?: throw NotFoundException("No uploadSession found in DB")
            else -> uploadController.getUploadByUUID(uuid)
                ?: throw NotFoundException("No uploadSession found in DB with uuid = $uuid")
        }

        val initUploadBody = InitUploadBody(uploadSession, recaptcha)
        val initUploadResponse = uploadRepository.initUpload(initUploadBody)
        uploadController.updateUploadSession(
            uuid = uploadSession.uuid,
            remoteContainer = initUploadResponse.container,
            remoteUploadHost = initUploadResponse.uploadHost,
            remoteFilesUUID = initUploadResponse.filesUUID,
        )

        return@withContext uploadController.getUploadByUUID(uploadSession.uuid)
    }

    /**
     * Uploads a chunk of data for a file in an upload session.
     *
     * @param uuid The UUID of the upload session.
     * @param fileUUID The UUID of the file being uploaded.
     * @param chunkIndex The index of the chunk being uploaded.
     * @param isLastChunk True if this is the last chunk of the file, false otherwise.
     * @param data The chunk data to upload.
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws NetworkException If there is a network error.
     * @throws UnknownException If an unknown error occurs.
     * @throws RealmException If an error occurs during database access.
     * @throws NotFoundException If we cannot find the upload session in the database with the specified uuid.
     * @throws NullPropertyException If remoteUploadHost or remoteContainer is null.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
        NotFoundException::class,
        NullPropertyException::class,
    )
    suspend fun uploadChunk(
        uuid: String,
        fileUUID: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        data: ByteArray,
        onUpload: (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): Unit = withContext(Dispatchers.IO) {
        val uploadSession = uploadController.getUploadByUUID(uuid)
            ?: throw NotFoundException("${UploadSession::class.simpleName} not found in DB with uuid = $uuid")
        val remoteUploadHost = uploadSession.remoteUploadHost
            ?: throw NullPropertyException("Remote upload host cannot be null")
        val remoteContainer = uploadSession.remoteContainer
            ?: throw NullPropertyException("Remote container cannot be null")

        uploadRepository.uploadChunk(
            uploadHost = remoteUploadHost,
            containerUUID = remoteContainer.uuid,
            fileUUID = fileUUID,
            chunkIndex = chunkIndex,
            isLastChunk = isLastChunk,
            data = data,
            onUpload = onUpload,
        )
    }

    /**
     * Finishes an upload session and add the transfer to the database.
     *
     * @param uuid The UUID of the upload session.
     *
     * @return The transfer UUID of the uploaded transfer.
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws NetworkException If there is a network error.
     * @throws UnknownException If an unknown error occurs.
     * @throws RealmException If an error occurs during database access.
     * @throws NotFoundException If we cannot find the upload session in the database with the specified uuid.
     * @throws NullPropertyException If remoteUploadHost or remoteContainer is null.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
        NotFoundException::class,
        NullPropertyException::class,
    )
    suspend fun finishUploadSession(uuid: String): String = withContext(Dispatchers.IO) {
        val uploadSession = uploadController.getUploadByUUID(uuid)
            ?: throw NotFoundException("Unknown upload session with uuid = $uuid")
        val containerUUID = uploadSession.remoteContainer?.uuid
            ?: throw NullPropertyException("Remote container cannot be null")

        val finishUploadBody = FinishUploadBody(
            containerUUID = containerUUID,
            language = uploadSession.language.code,
            recipientsEmails = uploadSession.recipientsEmails,
        )
        val finishUploadResponse = runCatching {
            uploadRepository.finishUpload(finishUploadBody).first()
        }.getOrElse { throw UnknownException(it) }

        uploadController.removeUploadSession(uuid)
        transferManager.addTransferByLinkUUID(finishUploadResponse.linkUUID)
        // TODO: If we can't retrieve the transfer cause of the Internet, we should put it in Realm and try again later.

        return@withContext finishUploadResponse.linkUUID // Here the linkUUID correspond to the transferUUID of a transferUI
    }

    /**
     * Deletes an upload session from the database.
     *
     * @param uuid The UUID of the upload session to delete.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        RealmException::class,
        CancellationException::class,
    )
    suspend fun deleteUploadSession(uuid: String): Unit = withContext(Dispatchers.IO) {
        uploadController.removeUploadSession(uuid)
    }
}
