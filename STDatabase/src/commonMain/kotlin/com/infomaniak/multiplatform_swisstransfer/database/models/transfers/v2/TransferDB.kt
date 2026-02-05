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

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.File
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus

@Entity
data class TransferDB(
    @PrimaryKey
    override val id: String,
    override val senderEmail: String,
    override val title: String?,
    override val message: String?,
    override val createdAt: Long,
    override val expiresAt: Long,
    override val totalSize: Long,
    // Local
    override val password: String? = null,
    override val transferDirection: TransferDirection,
    override val transferStatus: TransferStatus,
    override val recipientsEmails: Set<String> = emptySet(),
    val linkId: String? = null,
    val userOwnerId: Long,
) : Transfer {
    @Ignore
    override var files: List<File> = emptyList()
        internal set

    constructor(transfer: Transfer, linkId: String?, userOwnerId: Long) : this(
        id = transfer.id,
        senderEmail = transfer.senderEmail,
        title = transfer.title,
        message = transfer.message,
        createdAt = transfer.createdAt,
        expiresAt = transfer.expiresAt,
        totalSize = transfer.totalSize,
        password = transfer.password,
        transferDirection = transfer.transferDirection,
        transferStatus = transfer.transferStatus,
        recipientsEmails = transfer.recipientsEmails,
        linkId = linkId,
        userOwnerId = userOwnerId
    ) {
        this.files = transfer.files
    }
}

internal data class TransferWithFiles(
    @Embedded
    val transfer: TransferDB,
    @Relation(
        parentColumn = "id",
        entityColumn = "transferId"
    )
    val files: List<FileDB>
) {
    fun toTransferDB(): TransferDB = transfer.apply { files = this@TransferWithFiles.files }
}
