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
import io.realm.kotlin.ext.toRealmList

object FileUtils {

    fun getFileDBTree(containerUUID: String, files: List<File>): List<FileDB> {
        var tree = mutableListOf<FileDB>()
        for (file in files) {
            val fileDB = FileDB(file)
            val filePath = file.path

            if (filePath.isNullOrEmpty()) {
                tree.add(fileDB)
            } else {
                val pathComponents = filePath.split("/").toMutableList()
                val (modifiedTree, parent) = findFolder(
                    containerUUID = containerUUID,
                    pathComponents = pathComponents,
                    tree = tree
                )

                fileDB.parent = parent?.apply {
                    children.add(fileDB)
                    fileSizeInBytes = children.sumOf { it.fileSizeInBytes }
                    receivedSizeInBytes = children.sumOf { it.receivedSizeInBytes }
                }

                tree = modifiedTree.toMutableList()
            }
        }

        return tree
    }

    private fun findFolder(containerUUID: String, pathComponents: List<String>, tree: List<FileDB>): Pair<List<FileDB>, FileDB?> {
        val fakeFirstParent = FileDB(FileDB.FolderInfo(folderName = "", containerUUID = containerUUID))
        fakeFirstParent.children = tree.toRealmList()
        var currentParent = fakeFirstParent
        pathComponents.forEach { currentName ->
            // Update the current parent to the folder we found/created
            currentParent = currentParent.children.firstOrNull { it.fileName == currentName && it.isFolder } ?: run {
                val newFolder = FileDB(FileDB.FolderInfo(folderName = currentName, containerUUID = containerUUID))
                newFolder.parent = currentParent
                currentParent.children.add(newFolder)
                newFolder
            }
        }
        // Reassign the fake parent children to the tree and remove the fake parent link from the elements of the tree base
        val modifiedTree: List<FileDB> = fakeFirstParent.children
        modifiedTree.forEach {
            it.parent = null
        }
        return Pair(modifiedTree, currentParent)
    }
}
