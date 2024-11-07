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
package com.infomaniak.multiplatform_swisstransfer.common.utils

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.FileUi
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.UploadFile

object FileUtils {

    /**
     * Prepares a list of [UploadFile] objects for display in a hierarchical structure.
     *
     * This function takes a list of [UploadFile] objects and transforms them into a list of
     * [FileUi] objects, organizing them into a tree-like structure based on their file paths.
     *
     * Each [FileUi] represents a file or folder in the hierarchy. The function iterates
     * through the input [uploadFiles] and creates a corresponding [FileUi] for each.
     * It then determines the parent folder for each file based on its path and adds it to the
     * parent's children list. If a parent folder is not found, the file is added as a top-level
     * item in the tree.
     *
     * @param uploadFiles The list of [UploadFile] objects to prepare for display.
     * @return A list of [FileUi] objects representing the hierarchical structure of the files.
     */
    fun getFileUiTree(uploadFiles: List<UploadFile>): List<FileUi> {
        var tree = mutableListOf<FileUi>()
        for (file in uploadFiles) {
            val fileUi = FileUi(uploadFile = file)

            if (file.path.isEmpty()) {
                tree.add(fileUi)
            } else {
                val pathComponents = file.path.split("/").toMutableList()
                val result = findFolder(pathComponents = pathComponents, tree = tree)
                val parent = result.second
                fileUi.parent = parent
                parent.children.add(fileUi)
                tree = result.first.toMutableList()
            }
        }

        return tree
    }

    /**
     * Finds a folder within a hierarchical structure of [FileUi] objects.
     *
     * This function searches for a folder with a given path within a tree-like structure
     * represented by a list of [FileUi] objects. The path is specified as a list
     * of path components (e.g., ["folder1", "folder2", "file.txt"]).
     *
     * The function starts at the root of the tree and iteratively searches for each path
     * component in the children of the current folder. If a matching folder is found, it
     * becomes the current folder for the next iteration. If a matching folder is not found,
     * a new folder is created with the current path component name and added to the tree.
     *
     * The function returns the [FileUi] object representing the folder that matches
     * the given path, or null if no such folder is found.
     *
     * @param pathComponents A mutable list of strings representing the path components of the folder to find.
     * @param tree A mutable list of [FileUi] objects representing the hierarchical structure to search within.
     * @return The [FileUi] object representing the found folder, or null if not found.
     */
    fun findFolder(pathComponents: List<String>, tree: List<FileUi>): Pair<List<FileUi>, FileUi> {
        var result: FileUi? = null
        var modifiedTree = tree.toMutableList()

        val fakeFirstParent = FileUi(folderName = "")
        fakeFirstParent.children = modifiedTree
        var currentParent = fakeFirstParent

        pathComponents.forEach {
            val currentName = it

            currentParent.children.firstOrNull { it.fileName == currentName && it.isFolder }?.let { branch ->
                result = branch
            } ?: run {
                val newFolder = FileUi(folderName = currentName)
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
