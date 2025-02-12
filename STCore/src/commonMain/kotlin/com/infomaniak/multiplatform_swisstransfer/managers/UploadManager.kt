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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadDestination
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadFileSession
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSessionRequest
import com.infomaniak.multiplatform_swisstransfer.common.models.*
import com.infomaniak.multiplatform_swisstransfer.data.NewUploadSession
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.exceptions.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.exceptions.NullPropertyException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.*
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.InitUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ResendEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.VerifyEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.AuthorEmailToken
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.UploadCompleteResponse
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository
import com.infomaniak.multiplatform_swisstransfer.utils.EmailLanguageUtils
import kotlinx.coroutines.Dispatchers
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
    private val emailLanguageUtils: EmailLanguageUtils,
    private val emailTokensManager: EmailTokensManager,
) {

    val lastUploadFlow get() = uploadController.getLastUploadFlow()

    /**
     * Retrieves all upload sessions from the database.
     *
     * @return A list of all upload sessions.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getUploads(): List<UploadSession> = withContext(Dispatchers.Default) {
        return@withContext uploadController.getAllUploads()
    }

    /**
     * Retrieves the last upload session from the database.
     *
     * @return Last upload session.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getLastUpload(): UploadSession? = withContext(Dispatchers.Default) {
        return@withContext uploadController.getLastUpload()
    }

    /**
     * Retrieves the total number of uploads in the database.
     *
     * @return The number of uploads.
     * @throws RealmException If an error occurs during database access.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getUploadsCount(): Long = withContext(Dispatchers.Default) {
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
    suspend fun createAndGetUpload(newUploadSession: NewUploadSession): UploadSession = withContext(Dispatchers.Default) {
        uploadController.insertAndGet(newUploadSession)
    }

    /**
     * Stores the email address token in DB for future uses and updates the
     * current uploadSession stored in DB with this new email address token.
     *
     * @param authorEmail The email to which the [emailToken] is associated with.
     * @param emailToken The token returned by the API that proves the user owns [authorEmail].
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     * @throws NotFoundException If we can't find any upload to update.
     */
    @Throws(RealmException::class, CancellationException::class, NotFoundException::class)
    suspend fun updateAuthorEmailToken(authorEmail: String, emailToken: String) = withContext(Dispatchers.Default) {
        emailTokensManager.setEmailToken(authorEmail, emailToken)

        runCatching {
            uploadController.updateLastUploadSessionAuthorEmailToken(emailToken)
        }.onFailure {
            when (it) {
                is NoSuchElementException -> throw NotFoundException(it.message ?: "")
                else -> throw it
            }
        }
    }

    /**
     * Stores the email address token in DB for future uses.
     *
     * @param authorEmail The email to which the [emailToken] is associated with.
     * @param emailToken The token returned by the API that proves the user owns [authorEmail].
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun saveAuthorEmailToken(authorEmail: String, emailToken: String) {
        emailTokensManager.setEmailToken(authorEmail, emailToken)
    }

    /**
     * Instantiate a [NewUploadSession] and automatically fills in the author's
     * email token with the one associated with [authorEmail] from the data base.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun generateNewUploadSession(
        duration: ValidityPeriod,
        authorEmail: String,
        password: String,
        message: String,
        numberOfDownload: DownloadLimit,
        language: EmailLanguage,
        recipientsEmails: Set<String>,
        files: List<UploadFileSession>,
    ): NewUploadSession = NewUploadSession(
        duration = duration,
        authorEmail = authorEmail,
        authorEmailToken = emailTokensManager.getTokenForEmail(authorEmail),
        password = password,
        message = message,
        numberOfDownload = numberOfDownload,
        language = language,
        recipientsEmails = recipientsEmails,
        files = files,
    )

    /**
     * Initializes an upload session and update it in database with the remote data.
     *
     * @param uuid The UUID of the upload session or null to use the last upload session.
     * @param attestationHeaderName The name of the attestation header.
     * @param attestationToken The token generated by InfomaniakDeviceCheck / AppIntegrity.
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
    suspend fun initUploadSession(
        uuid: String? = null,
        attestationHeaderName: String,
        attestationToken: String,
    ): UploadSession? = withContext(Dispatchers.Default) {

        val uploadSession = when (uuid) {
            null -> uploadController.getLastUpload() ?: throw NotFoundException("No uploadSession found in DB")
            else -> uploadController.getUploadByUUID(uuid)
                ?: throw NotFoundException("No uploadSession found in DB with uuid = $uuid")
        }

        val initUploadBody = InitUploadBody(uploadSession)
        val initUploadResponse = uploadRepository.initUpload(initUploadBody, attestationHeaderName, attestationToken)
        uploadController.updateUploadSession(
            uuid = uploadSession.uuid,
            remoteContainer = initUploadResponse.container,
            remoteUploadHost = initUploadResponse.uploadHost,
            remoteFilesUUID = initUploadResponse.filesUUID,
        )

        return@withContext uploadController.getUploadByUUID(uploadSession.uuid)
    }

    /**
     * Initializes an upload session and update it in database with the remote data.
     *
     * @param attestationHeaderName The name of the attestation header.
     * @param attestationToken The token generated by InfomaniakDeviceCheck / AppIntegrity.
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
    suspend fun startUploadSession(
        request: UploadSessionRequest,
        attestationHeaderName: String,
        attestationToken: String,
    ): UploadDestination = withContext(Dispatchers.Default) {

        val initUploadBody = InitUploadBody(
            req = request,
            authorEmailToken = emailTokensManager.getTokenForEmail(request.authorEmail)
        )
        val initUploadResponse = uploadRepository.initUpload(initUploadBody, attestationHeaderName, attestationToken)

        UploadDestination(
            container = initUploadResponse.container,
            uploadHost = initUploadResponse.uploadHost,
            filesUuid = initUploadResponse.filesUUID
        )
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
        isRetry: Boolean,
        data: ByteArray,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): Unit = withContext(Dispatchers.Default) {
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
            isRetry = isRetry,
            data = data,
            onUpload = onUpload,
        )
    }

    /**
     * Uploads a chunk of data for a file in an upload session.
     *
     * @param uploadHost The host of the upload session.
     * @param remoteContainerUuid The id of the container returned by the backend earlier.
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
     * @throws NotFoundException If we cannot find the upload session in the database with the specified uuid.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        NotFoundException::class,
    )
    suspend fun uploadChunk(
        uploadHost: String,
        remoteContainerUuid: String,
        fileUUID: String,
        chunkIndex: Int,
        isLastChunk: Boolean,
        isRetry: Boolean,
        data: ByteArray,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): Unit = withContext(Dispatchers.Default) {
        uploadRepository.uploadChunk(
            uploadHost = uploadHost,
            containerUUID = remoteContainerUuid,
            fileUUID = fileUUID,
            chunkIndex = chunkIndex,
            isLastChunk = isLastChunk,
            isRetry = isRetry,
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
    suspend fun finishUploadSession(uuid: String): String = withContext(Dispatchers.Default) {
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
        }.getOrElse { exception ->
            throw if (exception is NoSuchElementException) UnknownException(exception) else exception
        }

        addTransferByLinkUUID(finishUploadResponse, uploadSession)
        uploadController.removeUploadSession(uuid)

        return@withContext finishUploadResponse.linkUUID // Here the linkUUID correspond to the transferUUID of a transferUI
    }

    /**
     * Finalizes an upload session and adds the transfer to the database.
     *
     * @param uploadSession The upload session to finalize and save.
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
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
        RealmException::class,
        NotFoundException::class,
    )
    suspend fun finalizeUploadSession(uploadSession: UploadSession): String {
        val containerUuid = requireNotNull(uploadSession.remoteContainer?.uuid) {
            "The remoteContainer property needs ot be specified."
        }
        return withContext(Dispatchers.Default) {
            val finishUploadBody = FinishUploadBody(
                containerUUID = containerUuid,
                language = uploadSession.language.code,
                recipientsEmails = uploadSession.recipientsEmails,
            )
            val finishUploadResponse = runCatching {
                uploadRepository.finishUpload(finishUploadBody).first()
            }.getOrElse { exception ->
                throw if (exception is NoSuchElementException) UnknownException(exception) else exception
            }

            addTransferByLinkUUID(finishUploadResponse, uploadSession)

            finishUploadResponse.linkUUID // Here the linkUUID corresponds to the transferUUID of a transferUI
        }
    }

    /**
     * Cancel an upload session from api and remove it.
     *
     * @param uuid The UUID of the upload session to cancel.
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
    suspend fun cancelUploadSession(uuid: String): Unit = withContext(Dispatchers.Default) {
        val uploadSession = uploadController.getUploadByUUID(uuid)
            ?: throw NotFoundException("Unknown upload session with uuid = $uuid")
        val containerUUID = uploadSession.remoteContainer?.uuid
            ?: throw NullPropertyException("Remote container cannot be null")

        uploadController.removeUploadSession(uuid)
        uploadRepository.cancelUpload(containerUUID)
    }

    /**
     * Cancel an upload session from api and remove it.
     *
     * @param containerUUID The UUID of the upload session's container to cancel.
     *
     * @throws CancellationException If the operation is cancelled.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws NetworkException If there is a network error.
     * @throws UnknownException If an unknown error occurs.
     */
    @Throws(
        CancellationException::class,
        ApiException::class,
        UnexpectedApiErrorFormatException::class,
        NetworkException::class,
        UnknownException::class,
    )
    suspend fun cancelUploadSessionByContainerId(containerUUID: String): Unit = withContext(Dispatchers.Default) {
        uploadRepository.cancelUpload(containerUUID)
    }

    /**
     * Remove an upload session from the database.
     *
     * @param uuid The UUID of the upload session to remove.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        RealmException::class,
        CancellationException::class,
    )
    suspend fun removeUploadSession(uuid: String): Unit = withContext(Dispatchers.Default) {
        uploadController.removeUploadSession(uuid)
    }

    @Throws(
        RealmException::class,
        CancellationException::class,
    )
    suspend fun removeAllUploadSession(): Unit = withContext(Dispatchers.Default) {
        uploadController.removeData()
    }

    @Throws(
        CancellationException::class,
        EmailValidationException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun verifyEmailCode(code: String, address: String): AuthorEmailToken = withContext(Dispatchers.Default) {
        return@withContext uploadRepository.verifyEmailCode(VerifyEmailCodeBody(code, address))
    }

    @Throws(
        CancellationException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun resendEmailCode(address: String): Unit = withContext(Dispatchers.Default) {
        val language = emailLanguageUtils.getEmailLanguageFromLocal()
        uploadRepository.resendEmailCode(ResendEmailCodeBody(address, language.code))
    }

    private suspend fun addTransferByLinkUUID(
        finishUploadResponse: UploadCompleteResponse,
        uploadSession: UploadSession,
    ) {
        runCatching {
            transferManager.addTransferByLinkUUID(
                linkUUID = finishUploadResponse.linkUUID,
                password = uploadSession.password,
                recipientsEmails = uploadSession.recipientsEmails,
                transferDirection = TransferDirection.SENT,
            )
        }.onFailure { exception ->
            if (exception is FetchTransferException) {
                createTransferLocally(exception, finishUploadResponse, uploadSession)
            } else {
                throw exception
            }
        }
    }

    private suspend fun createTransferLocally(
        exception: FetchTransferException,
        finishUploadResponse: UploadCompleteResponse,
        uploadSession: UploadSession,
    ) {
        val transferStatus = when {
            exception is FetchTransferException.VirusCheckFetchTransferException -> TransferStatus.WAIT_VIRUS_CHECK
            else -> TransferStatus.UNKNOWN
        }
        transferManager.createTransferLocally(finishUploadResponse.linkUUID, uploadSession, transferStatus)
    }
}
