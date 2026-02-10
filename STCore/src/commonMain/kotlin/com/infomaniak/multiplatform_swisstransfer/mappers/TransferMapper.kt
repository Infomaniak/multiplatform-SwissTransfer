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
package com.infomaniak.multiplatform_swisstransfer.mappers

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.FileUi
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.TransferUi
import com.infomaniak.multiplatform_swisstransfer.database.dao.TransferDao
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal suspend fun TransferDB.toTransferUi(transferDao: TransferDao): TransferUi {
    // Fetch files for this transfer
    val files = transferDao.getTransferRootFiles(this.id)

    return TransferUi(
        uuid = this.id,
        createdDateTimestamp = this.createdAt,
        expirationDateTimestamp = this.expiresAt,
        sizeUploaded = this.totalSize,
        downloadLimit = 1, // TODO[API-V2]: Not available in v2 model
        downloadLeft = 1,  // TODO[API-V2]: Not available in v2 model
        message = this.message,
        password = this.password,
        recipientsEmails = this.recipientsEmails,
        files = files.map { it.toFileUi() },
        direction = this.transferDirection,
        transferStatus = this.transferStatus,
    )
}

internal fun FileDB.toFileUi(): FileUi = FileUi(
    uid = this.id,
    fileName = this.path.substringAfterLast("/"),
    path = this.path,
    isFolder = this.isFolder,
    fileSize = this.size,
    mimeType = this.mimeType,
    localPath = null,
    thumbnailPath = this.thumbnailPath,
)

internal suspend fun List<TransferDB>.toTransferUiList(transferDao: TransferDao): List<TransferUi> {
    return this.map { it.toTransferUi(transferDao) }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal fun Flow<List<TransferDB>>.toTransferUiListFlow(transferDao: TransferDao): Flow<List<TransferUi>> {
    return this.mapLatest { it.toTransferUiList(transferDao) }
}

internal fun List<FileDB>.toFileUiList(): List<FileUi> {
    return this.map { it.toFileUi() }
}
