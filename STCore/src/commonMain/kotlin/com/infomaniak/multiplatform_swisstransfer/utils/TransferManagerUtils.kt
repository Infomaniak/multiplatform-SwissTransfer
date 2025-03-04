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
package com.infomaniak.multiplatform_swisstransfer.utils

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.managers.TransferManager
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.FetchTransferException
import com.infomaniak.multiplatform_swisstransfer.network.models.upload.response.UploadCompleteResponse

internal suspend fun TransferManager.addTransferByLinkUUID(
    finishUploadResponse: UploadCompleteResponse,
    uploadSession: UploadSession,
) {
    runCatching {
        addTransferByLinkUUID(
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

private suspend fun TransferManager.createTransferLocally(
    exception: FetchTransferException,
    finishUploadResponse: UploadCompleteResponse,
    uploadSession: UploadSession,
) {
    val transferStatus = when {
        exception is FetchTransferException.VirusCheckFetchTransferException -> TransferStatus.WAIT_VIRUS_CHECK
        else -> TransferStatus.UNKNOWN
    }
    createTransferLocally(finishUploadResponse.linkUUID, uploadSession, transferStatus)
}
