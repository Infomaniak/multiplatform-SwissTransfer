/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2026 Infomaniak Network SA
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

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSessionRequest
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.dao.TransferDao
import com.infomaniak.multiplatform_swisstransfer.database.dao.UploadDao
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtilsForApiV2
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiV2ErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.ChunkEtag
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.CreateTransfer
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.request.v2.TransferFile
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadV2Repository
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.cancellation.CancellationException

/**
 * Manages upload operations for SwissTransfer.
 *
 * This class handles the initialization, progress, and completion of uploads,
 * interacting with the database and the network repository.
 *
 * @property uploadRepository The repository for interacting with the SwissTransfer API for uploads.
 * @property transferManager Transfer operations
 */
class UploadV2Manager(
    private val accountManager: AccountManager,
    private val uploadRepository: UploadV2Repository,
    private val transferManager: TransferManager,
    private val appDatabase: AppDatabase,
    private val uploadDao: UploadDao,
    private val transferDao: TransferDao,
) {

    private fun requireCurrentUserId(): Long {
        return (accountManager.currentUser as? STUser.AuthUser)?.id
            ?: throw UnknownException(Exception("No user logged in"))
    }

    /**
     * Prepare a transfer to be uploaded.
     *
     * @return The [Transfer] object to use for [uploadFileChunk] and [uploadFile],
     * or [getUploadFileChunkUrl] and [getUploadFileUrl]
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @OptIn(ExperimentalContracts::class)
    @Throws(
        CancellationException::class,
        ApiV2ErrorException::class,
        NetworkException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun prepareTransfer(
        request: UploadSessionRequest,
    ): Transfer = withContext(Dispatchers.Default) {
        val userId = requireCurrentUserId()
        transferDao.deleteAnyPendingTransfer(userId)

        val transferCreationPayload = CreateTransfer(
            title = null, //TODO[ST-v2]: Add title
            message = request.message,
            password = request.password,
            language = request.languageCode.code.substringBefore('_'),
            expiresInDays = request.validityPeriod.days,
            maxDownload = request.downloadCountLimit.value,
            files = request.filesMetadata.map {
                TransferFile(
                    path = it.name,
                    size = it.size,
                    mimeType = it.mimeType
                )
            },
            recipients = request.recipientsEmails.toList()
        )
        uploadRepository.createTransfer(transferCreationPayload).also { apiTransfer ->
            val transferToPersist = TransferDB(transfer = apiTransfer, linkId = null, userOwnerId = userId)
            val filesToInsert = FileUtilsForApiV2.getFileDbTree(
                transferId = apiTransfer.id,
                files = apiTransfer.files
            )
            appDatabase.useWriterConnection {
                it.immediateTransaction {
                    transferDao.upsertTransfer(transferToPersist)
                    filesToInsert.forEach { file -> transferDao.upsertFile(file) }
                }
            }
        }
    }

    /**
     * Returns a transfer previously created through [prepareTransfer], that hasn't been finalized yet.
     */
    @Throws(CancellationException::class)
    suspend fun getPendingTransferIfAny(): Transfer? {
        val userId = requireCurrentUserId()
        return transferDao.getPendingTransfer(userId)
    }

    /**
     * Uploads a chunk of data for a file in a given transfer.
     *
     * @param transferId The id of the transfer created in [prepareTransfer].
     * @param fileId The id of the file being uploaded.
     * @param chunkIndex The index of the chunk being uploaded.
     * @param data The chunk data to upload.
     *
     * @return The etag of the uploaded chunk.
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun uploadFileChunk(
        transferId: String,
        fileId: String,
        chunkIndex: Int,
        data: OutgoingContent.WriteChannelContent,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): String = withContext(Dispatchers.Default) {
        val cephUrl = getUploadFileChunkUrl(transferId = transferId, fileId = fileId, chunkIndex = chunkIndex)
        uploadRepository.uploadChunkToEtag(
            cephUrl = cephUrl,
            data = data,
            onUpload = onUpload,
        ) ?: throw UnknownException(Exception("No etag returned. Uncaught unsuccessful response?"))
    }

    /**
     * Returns the url where to upload a chunk of data for a file in a given transfer.
     *
     * @param transferId The id of the transfer created in [prepareTransfer].
     * @param fileId The UUID of the file to upload.
     * @param chunkIndex The index of the chunk to upload.
     *
     * @return The target url
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun getUploadFileChunkUrl(
        transferId: String,
        fileId: String,
        chunkIndex: Int,
    ): String = uploadRepository.getPresignedUploadChunkUrl(
        transferId = transferId,
        fileId = fileId,
        chunkIndex = chunkIndex
    ).url

    /**
     * Finalizes a file after all its chunks have successfully been uploaded.
     *
     * @param transferId The id of the transfer created in [prepareTransfer].
     * @param fileId The id of the file to upload.
     * @param etags The etags from the previously uploaded chunks, returned by [uploadFileChunk],
     * or the completion of the url returned by [getUploadFileChunkUrl].
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun finalizeFileUploadedInChunks(
        transferId: String,
        fileId: String,
        etags: List<ChunkEtag>,
    ) {
        val succeeded = uploadRepository.finalizeTransferFile(
            transferId = transferId,
            fileId = fileId,
            etags = etags
        )
        if (!succeeded) {
            throw UnknownException(Exception("Unknown error while finalizing the file with id $fileId in transfer $transferId"))
        }
    }

    /**
     * Uploads all the data of a file in a given transfer.
     *
     * @param transferId The id of the transfer created in [prepareTransfer].
     * @param fileId The id of the file being uploaded.
     * @param data The chunk data to upload.
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
    )
    suspend fun uploadFile(
        transferId: String,
        fileId: String,
        data: OutgoingContent.WriteChannelContent,
        onUpload: suspend (bytesSentTotal: Long, chunkSize: Long) -> Unit,
    ): Unit = withContext(Dispatchers.Default) {
        val cephUrl = getUploadFileUrl(transferId = transferId, fileId = fileId)
        uploadRepository.uploadFile(
            cephUrl = cephUrl,
            data = data,
            onUpload = onUpload,
        )
    }

    /**
     * Returns the url where to upload an entire file in a given transfer.
     *
     * @param transferId The id of the transfer created in [prepareTransfer].
     * @param fileId The id of the file to upload.
     *
     * @return The target url
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        CancellationException::class,
    )
    suspend fun getUploadFileUrl(
        transferId: String,
        fileId: String,
    ): String = uploadRepository.getPresignedUploadUrl(
        transferId = transferId,
        fileId = fileId,
    ).url

    /**
     * Cancels an upload session and remove the transfer from the database.
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        CancellationException::class,
    )
    suspend fun cancelTransfer(transferId: String, failed: Boolean): Boolean {
        return when (failed) {
            true -> uploadRepository.markTransferAsFailed(transferId)
            false -> uploadRepository.cancelTransfer(transferId)
        }.also { transferDao.deleteTransfer(transferId = transferId) }
    }

    /**
     * Finalizes an upload session and adds the transfer to the database.
     *
     * @see com.infomaniak.multiplatform_swisstransfer.SharedApiUrlCreator.shareTransferV2Url
     *
     * @throws NetworkException If there is a network error.
     * @throws ApiV2ErrorException If there is a general API error.
     * @throws UnexpectedApiErrorFormatException If the API error format is unexpected.
     * @throws UnknownException If an unknown error occurs.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        CancellationException::class,
    )
    suspend fun finalizeTransferAndGetLinkUuid(transferId: String): String {
        val userId = requireCurrentUserId()
        return uploadRepository.finalizeTransferAndGetLinkUuid(transferId).also {
            transferDao.markPendingTransferAsReady(userId = userId, transferId = transferId)
        }
    }
}
