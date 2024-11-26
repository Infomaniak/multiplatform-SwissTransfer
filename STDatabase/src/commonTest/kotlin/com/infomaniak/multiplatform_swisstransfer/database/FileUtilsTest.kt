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
package com.infomaniak.multiplatform_swisstransfer.database

import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtils
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileUtilsTest {

    private val tree = mutableListOf<FileDB>()

    @AfterTest
    fun clean() {
        tree.clear()
    }

    @Test
    fun filesInRoot() {
        val filesList = listOf(
            FileDB(fileName = "file1.txt", path = ""),
            FileDB(fileName = "file2.txt", path = ""),
        )
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))

        assertNotNull(tree.getFileDB("file1.txt"), "file1.txt should exist")
        assertNotNull(tree.getFileDB("file2.txt"), "file2.txt should exist")
    }

    @Test
    fun filesInFolder() {
        val filesList = listOf(
            FileDB(fileName = "file3.txt", path = "folder1"),
            FileDB(fileName = "file4.txt", path = "folder1"),
            FileDB(fileName = "empty_file.txt", path = "folder1/randomFolder"),
            FileDB(fileName = "file5.txt", path = "folder2"),
            FileDB(fileName = "file6.txt", path = "folder2"),
        )
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))

        checkFolders(path = "folder1/randomFolder", files = tree)

        // Files in folder1
        val folder1 = tree.getFileDB("folder1")
        val folder1Children = folder1?.children?.filter { !it.isFolder }
        assertTrue(
            actual = folder1Children?.size == 2,
            message = "Children of folder1 should contain two files",
        )
        assertNotNull(
            actual = folder1Children?.getFileDB("file3.txt"),
            message = "file3.txt is missing from folder1",
        )
        assertNotNull(
            actual = folder1Children?.getFileDB("file4.txt"),
            message = "file4.txt is missing from folder1",
        )

        // Files in folder2
        val folder2 = tree.getFileDB("folder2")
        assertNotNull(folder2, "folder2 doesn't exist")
        assertTrue(
            actual = folder2.children.none { it.isFolder },
            message = "Children of folder2 should not contain any folders",
        )

        val folder2Children = folder2.children.filter { !it.isFolder }
        assertTrue(
            actual = folder2Children.size == 2,
            message = "Children of folder2 should contain two files",
        )
        assertNotNull(
            actual = folder2Children.getFileDB("file5.txt"),
            message = "file5.txt in folder2 does not exist",
        )
        assertNotNull(
            actual = folder2Children.getFileDB("file6.txt"),
            message = "file6.txt in folder2 does not exist",
        )
    }

    @Test
    fun fileContainedIn2NestedFolders() {
        val path = "folder1/folder2"
        val filesList = listOf(FileDB(fileName = "file_in_folder1_folder2.txt", path = path))
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))

        checkFolders(path = path, files = tree)

        // File contained in one folder contained in another folder
        val folder2InFolder1 = tree.getFileDB("folder1")?.children?.getFileDB("folder2")
        assertNotNull(
            actual = folder2InFolder1?.children?.getFileDB("file_in_folder1_folder2.txt"),
            message = "file_in_folder1_folder2.txt does not exist in folder1/folder2",
        )
    }

    @Test
    fun fileContainedIn5NestedFolders() {
        val path = "folder1/folder2/folder3/folder4/folder5"
        val filesList = listOf(
            FileDB(
                fileName = "file_in_folder1_folder2_folder3_folder4_folder5.txt",
                path = path,
            )
        )
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))

        checkFolders(path = path, files = tree)

        val folder5 = getFolder(path, tree)
        assertNotNull(
            actual = folder5?.children?.getFileDB("file_in_folder1_folder2_folder3_folder4_folder5.txt"),
            message = "file_in_folder1_folder2_folder3_folder4_folder5.txt does not exist in $path",
        )
    }

    @Test
    fun fileVeryDeepInFoldersTree() {
        val path =
            "folder1/folder2/folder3/folder4/folder5/folder6/folder7/folder8/folder9/folder10/folder11/folder12/folder13/folder14/folder15/folder16/folder17/folder18/folder19/folder20/folder21/folder22/folder23/folder24/folder25"
        val filesList = listOf(
            FileDB(
                fileName = "hidden_file.txt",
                path = path
            )
        )
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))

        checkFolders(path = path, files = tree)

        val folder25 = getFolder(path, tree)
        assertNotNull(
            actual = folder25?.children?.getFileDB("hidden_file.txt"),
            message = "folder25 should contain hidden_file.txt",
        )
    }

    private tailrec fun getFolder(folderPath: String, files: List<FileDB>): FileDB? {
        if (folderPath.isEmpty()) return null

        val (folderName, remainingPath) = getInfoFromPath(folderPath)
        val currentFolder = files.getFileDB(folderName) ?: return null

        return if (remainingPath.isNotEmpty()) {
            getFolder(remainingPath, currentFolder.children)
        } else {
            currentFolder
        }
    }

    private tailrec fun checkFolders(path: String, files: List<FileDB>) {
        if (path.isEmpty()) return

        val (folderName, remainingPath) = getInfoFromPath(path)
        val foundFolder = files.getFileDB(folderName)
        assertNotNull(foundFolder, "$folderName should not be null")

        if (remainingPath.isEmpty()) return

        checkFolders(path = remainingPath, files = foundFolder.children)
    }

    private fun getInfoFromPath(path: String): Pair<String, String> {
        return if (path.contains("/")) {
            val result = path.split("/", limit = 2)
            result[0] to result[1]
        } else {
            Pair(path, "")
        }
    }

    private fun List<FileDB>.getFileDB(name: String) = find { it.fileName == name }
}
