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
package com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.RemoteUploadFile
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadFileSession
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class FileUi(
    val uid: String,
    val fileName: String,
    val isFolder: Boolean,
    val fileSize: Long,
    val mimeType: String?,
    val localPath: String?,
    var parent: File? = null,
    var children: MutableList<File> = mutableListOf(),
) {

    constructor(file: File) : this(
        uid = file.uuid,
        fileName = file.fileName,
        isFolder = false,
        fileSize = file.receivedSizeInBytes,
        mimeType = file.mimeType,
        localPath = null,
    )

    // Init for folder
    constructor(folderName: String) : this(
        uid = Uuid.random().toString(),
        fileName = folderName,
        isFolder = true,
        fileSize = 0L,
        mimeType = null,
        localPath = null,
    )

    constructor(uploadFile: UploadFile) : this(
        uid = uploadFile.url,
        fileName = uploadFile.url.substringAfterLast("/"),
        isFolder = false,
        fileSize = 0L,
        mimeType = null,
        localPath = uploadFile.url,
    )

    fun toUploadFileSession(): UploadFileSession {
        return object : UploadFileSession {
            override val name: String = fileName
            override val size: Long = fileSize
            override val mimeType: String = this@FileUi.mimeType ?: ""
            override val localPath: String = this@FileUi.localPath ?: ""
            override val remoteUploadFile: RemoteUploadFile? = null
        }
    }

    override fun toString() = "$fileName -> children: ${children.map { it.fileName }}"
}
