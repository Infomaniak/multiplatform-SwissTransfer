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
package com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DownloadManagerRef private constructor(
    @PrimaryKey
    val id: String,
    val transferId: String,
    @Suppress("unused") // We prefer to keep it in the database.
    val fileId: String?,
    val downloadManagerUniqueId: Long,
    val userOwnerId: Long,
) {

    constructor(
        transferId: String,
        fileUid: String?,
        downloadManagerUniqueId: Long,
        userOwnerId: Long,
    ) : this(
        id = buildPrimaryKeyForRealm(transferId, fileUid),
        transferId = transferId,
        fileId = fileUid,
        downloadManagerUniqueId = downloadManagerUniqueId,
        userOwnerId = userOwnerId,
    )

    companion object Companion {
        fun buildPrimaryKeyForRealm(transferId: String, fileId: String?): String {
            return if (fileId == null) transferId else "$transferId $fileId"
        }
    }
}
