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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.FileDB
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList

object FileUtils {

    fun getFileDBTree(containerUUID: String, files: List<File>): List<FileDB> {
        val fileTree = realmListOf<FileDB>()

        files.forEach { file ->
            val fileDB = FileDB(file)
            val filePath = file.path

            // filePath is null, meaning we can add the file to the root of the file tree
            if (filePath.isNullOrEmpty()) {
                fileTree.add(fileDB)
            } else {
                fileTree.addFileToFolder(
                    containerUUID = containerUUID,
                    pathComponents = filePath.split("/").toMutableList(),
                    file = fileDB,
                )
            }
        }

        return fileTree
    }

    private fun RealmList<FileDB>.addFileToFolder(
        containerUUID: String,
        pathComponents: List<String>,
        file: FileDB,
    ) {
        // Initializing folder with a file representing the root of the file tree
        var parentFolder = FileDB.newFolder(folderName = "", containerUUID = containerUUID).apply {
            children = this@addFileToFolder
        }

        // Iterating through pathComponents to create the folder to the right place
        pathComponents.forEach { currentName ->
            val existingFolder = parentFolder.children.find { it.fileName == currentName && it.isFolder }

            parentFolder = existingFolder ?: createFolder(currentName, containerUUID).apply { parentFolder.children.add(this) }
        }

        parentFolder.apply {
            children.add(file)
            computeFolderSize(file)
        }
    }

    private fun createFolder(folderName: String, containerUUID: String): FileDB {
        return FileDB.newFolder(folderName = folderName, containerUUID = containerUUID)
    }

    // Computed sizes here might be incorrect, especially if the folder contain sub-folders
    private fun FileDB.computeFolderSize(fileToAdd: FileDB) {
        fileSizeInBytes += fileToAdd.fileSizeInBytes
        receivedSizeInBytes += fileToAdd.receivedSizeInBytes
    }
}
