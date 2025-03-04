/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSessionRequest
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.exceptions.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.*
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.FinishUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.InitUploadBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.ResendEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.VerifyEmailCodeBody
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.AuthorEmailToken
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository
import com.infomaniak.multiplatform_swisstransfer.utils.EmailLanguageUtils
import com.infomaniak.multiplatform_swisstransfer.utils.addTransferByLinkUUID
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
class InMemoryUploadManager(
    private val uploadController: UploadController,
    private val uploadRepository: UploadRepository,
    private val transferManager: TransferManager,
    private val emailLanguageUtils: EmailLanguageUtils,
    private val emailTokensManager: EmailTokensManager,
) {

    /**
     * Starts an upload session and returns the upload destination for use by [uploadChunk].
     *
     * @param attestationHeaderName The name of the attestation header.
     * @param attestationToken The token generated by InfomaniakDeviceCheck / AppIntegrity.
     *
     * @return The [UploadDestination] with the container, host, and file uuids info.
     *
     * @throws ContainerErrorsException If there is an error with the container.
     * @throws NetworkException If there is a network error.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws RealmException If an error occurs during database access.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
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

    @Throws(
        CancellationException::class,
        EmailValidationException::class,
        ApiException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun verifyEmailCode(code: String, address: String): AuthorEmailToken = withContext(Dispatchers.Default) {
        uploadRepository.verifyEmailCode(VerifyEmailCodeBody(code, address))
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

    /**
     * Stores the email address token in DB for use by [startUploadSession].
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
     * Uploads a chunk of data for a file in the given container at the given host
     *
     * @param uploadHost The host of the upload session.
     * @param remoteContainerUuid The id of the container returned by the backend earlier.
     * @param fileUUID The UUID of the file being uploaded.
     * @param chunkIndex The index of the chunk being uploaded.
     * @param isLastChunk True if this is the last chunk of the file, false otherwise.
     * @param data The chunk data to upload.
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws NotFoundException If we cannot find the upload session in the database with the specified uuid.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
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
        data: ByteArray,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): Unit = withContext(Dispatchers.Default) {
        uploadRepository.uploadChunk(
            uploadHost = uploadHost,
            containerUUID = remoteContainerUuid,
            fileUUID = fileUUID,
            chunkIndex = chunkIndex,
            isLastChunk = isLastChunk,
            isRetry = false,
            data = data,
            onUpload = onUpload,
        )
    }

    /**
     * Requests cancelling the upload session in the backend.
     *
     * @param containerUUID The UUID of the upload session's container to cancel.
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
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
     * Finalizes an upload session and adds the transfer to the database.
     *
     * @param uploadSession The upload session to finalize and save.
     *
     * @return The transfer UUID of the uploaded transfer.
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws NotFoundException If we cannot find the upload session in the database with the specified uuid.
     * @throws UnknownException If an unknown error occurs.
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
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

            transferManager.addTransferByLinkUUID(finishUploadResponse, uploadSession)

            finishUploadResponse.linkUUID // Here the linkUUID corresponds to the transferUUID of a transferUI
        }
    }
}
