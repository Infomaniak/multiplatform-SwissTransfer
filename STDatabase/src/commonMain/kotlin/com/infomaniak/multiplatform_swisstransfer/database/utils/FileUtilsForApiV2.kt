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
package com.infomaniak.multiplatform_swisstransfer.database.utils

import androidx.collection.ArraySet
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.File
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.FileDB
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object FileUtilsForApiV2 {

    fun getFileDbTree(transferId: String, files: List<File>): Set<FileDB> {
        val folderByPath = HashMap<String, FileDB>(files.size)
        val out = ArraySet<FileDB>(files.size * 2)

        for (file in files) {
            val parentId = ensureFolders(file.path, file.size, transferId, folderByPath, out)
            out += FileDB(file, transferId, parentId)
        }
        return out
    }

    private fun ensureFolders(
        filePath: String,
        fileSize: Long,
        transferId: String,
        folderByPath: MutableMap<String, FileDB>,
        out: MutableSet<FileDB>,
    ): String? {
        val lastSlash = filePath.lastIndexOf('/')
        if (lastSlash <= 0) return null // No parent folder (root file)

        var parentId: String? = null

        filePath.loopOverParentFolders { folderPath ->
            val folder = folderByPath[folderPath] ?: generateFolderId(transferId).let { newId ->
                FileDB(
                    id = newId,
                    path = folderPath,
                    size = 0L,
                    mimeType = null,
                    isFolder = true,
                    transferId = transferId,
                    parentFolderId = parentId
                ).also {
                    folderByPath[folderPath] = it
                    out += it
                }
            }

            folder.size += fileSize

            parentId = folder.id
        }

        return parentId
    }

    /**
     * Will loop over parent folders based on an input path in the form of a string
     *
     * "abc/def/ghi/jkl.txt" will give "abc" then "abc/def" and then "abc/def/ghi"
     */
    private fun String.loopOverParentFolders(action: (String) -> Unit) {
        val directoryPath = this.substringBeforeLast('/', "")
        if (directoryPath.isEmpty()) return

        var index = directoryPath.indexOf('/')
        while (index != -1) {
            action(directoryPath.substring(0, index))
            index = directoryPath.indexOf('/', index + 1)
        }

        action(directoryPath)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateFolderId(transferId: String): String {
        return "$transferId:${Uuid.random()}"
    }
}
