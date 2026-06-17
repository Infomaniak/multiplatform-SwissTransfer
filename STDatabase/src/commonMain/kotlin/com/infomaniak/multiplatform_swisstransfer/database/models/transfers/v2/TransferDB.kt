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
package com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus

@Entity
data class TransferDB(
    @PrimaryKey
    val id: String,
    val senderEmail: String,
    val title: String?,
    val message: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val totalSize: Long,
    // Local
    val password: String? = null,
    val transferDirection: TransferDirection,
    val transferStatus: TransferStatus,
    val recipientsEmails: Set<String> = emptySet(),
    val linkId: String? = null,
    val userOwnerId: Long,
    //TODO[ST-v2]: Add language
) {
    @Ignore
    var files: List<FileDB> = emptyList()
        internal set
}
