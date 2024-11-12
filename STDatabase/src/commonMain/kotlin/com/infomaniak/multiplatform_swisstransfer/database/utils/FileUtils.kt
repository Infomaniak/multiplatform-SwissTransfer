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

    fun getFileDBTree(files: List<File>): List<FileDB> {
        var tree = mutableListOf<FileDB>()
        for (file in files) {
            val fileDB = FileDB(file)

            if (file.path?.isEmpty() == true) {
                tree.add(fileDB)
            } else {
                val pathComponents = file.path?.split("/")?.toMutableList() ?: emptyList()
                val result = findFolder(pathComponents = pathComponents, tree = tree)
                val parent = result.second
                fileDB.parent = parent
                parent?.children?.add(fileDB)
                parent?.fileSizeInBytes = parent?.children?.sumOf { it.fileSizeInBytes } ?: 0L
                parent?.receivedSizeInBytes = parent?.children?.sumOf { it.receivedSizeInBytes } ?: 0L

                tree = result.first.toMutableList()
            }
        }

        return tree
    }

    private fun findFolder(pathComponents: List<String>, tree: List<FileDB>): Pair<List<FileDB>, FileDB?> {
        var result: FileDB? = null
        var modifiedTree = tree.toMutableList()

        val fakeFirstParent = FileDB(folderName = "")
        fakeFirstParent.children = modifiedTree.toRealmList()
        var currentParent = fakeFirstParent

        pathComponents.forEach {
            val currentName = it

            currentParent.children.firstOrNull { it.fileName == currentName && it.isFolder }?.let { branch ->
                result = branch
            } ?: run {
                val newFolder = FileDB(folderName = currentName)
                newFolder.parent = currentParent
                currentParent.children.add(newFolder)

                result = newFolder
            }

            // Update the current parent to the folder we found/created
            currentParent = result!!
        }

        // Reassign the fake parent children to the tree and remove the fake parent link from the elements of the tree base
        modifiedTree = fakeFirstParent.children
        modifiedTree.forEach {
            it.parent = null
        }

        return Pair(modifiedTree, result!!)
    }
}
