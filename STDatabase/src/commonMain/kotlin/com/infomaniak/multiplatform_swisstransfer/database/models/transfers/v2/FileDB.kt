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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.File

@Entity(
    foreignKeys = [ForeignKey(
        entity = TransferDB::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("transferId"),
        onDelete = ForeignKey.CASCADE,
    )]
)
data class FileDB(
    @PrimaryKey
    override val id: String,
    override val path: String,
    override var size: Long,
    override val mimeType: String?,
    //Local
    override val isFolder: Boolean,
    override val thumbnailPath: String? = null,
    @ColumnInfo(index = true)
    val transferId: String,
    val folderId: String? = null,
) : File {
    @Ignore
    val children: List<FileDB> = emptyList()
    @Ignore
    val folder: FileDB? = null

    constructor(file: File, transferId: String, folderId: String?) : this(
        id = file.id,
        path = file.path,
        size = file.size,
        mimeType = file.mimeType,
        isFolder = file.isFolder,
        transferId = transferId,
        folderId = folderId
    )
}
