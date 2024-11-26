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
import kotlin.test.*

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

        // Files in folder1
        findFirstChildByNameInList(tree, "folder1")?.let { folder1 ->
            assertTrue(
                actual = folder1.children.filter { it.isFolder }.size == 1,
                message = "Children of folder1 should contain one folder",
            )
            val innerFiles = folder1.children.filter { !it.isFolder }
            assertTrue(
                actual = innerFiles.size == 2,
                message = "Children of folder1 should contain two files",
            )
            assertTrue(
                actual = innerFiles.find { it.fileName == "file3.txt" } != null,
                message = "Children of folder1 should contain two files",
            )
            assertTrue(
                actual = innerFiles.find { it.fileName == "file4.txt" } != null,
                message = "file4.txt is missing from folder1",
            )
        } ?: fail("folder1 doesn't exist")


        // Files in folder2
        findFirstChildByNameInList(tree, "folder2")?.let { folder2 ->
            assertTrue(
                actual = folder2.children.none { it.isFolder },
                message = "Children of folder2 should not contain any folders",
            )

            val innerFiles = folder2.children.filter { !it.isFolder }
            assertTrue(
                actual = innerFiles.size == 2,
                message = "Children of folder2 should contain two files",
            )
            assertTrue(
                actual = innerFiles.find { it.fileName == "file5.txt" } != null,
                message = "file5.txt in folder2 does not exist",
            )
            assertTrue(
                actual = innerFiles.find { it.fileName == "file6.txt" } != null,
                message = "file6.txt in folder2 does not exist",
            )
        } ?: fail("folder2 doesn't exist")
    }

    @Test
    fun fileContainedIn2NestedFolders() {
        val filesList = listOf(FileDB(fileName = "file_in_folder1_folder2.txt", path = "folder1/folder2"))
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))

        // File contained in one folder contained in another folder
        findFirstChildByNameInList(tree, "folder2")?.let { folder2InFolder1 ->
            assertTrue(
                actual = folder2InFolder1.children.find { it.fileName == "file_in_folder1_folder2.txt" } != null,
                message = "file_in_folder1_folder2.txt does not exist in folder1/folder2",
            )
        } ?: fail("folder2 in folder1 doesn't exist")
    }

    @Test
    fun fileContainedIn5NestedFolders() {
        val filesList = listOf(
            FileDB(
                fileName = "file_in_folder1_folder2_folder3_folder4_folder5.txt",
                path = "folder1/folder2/folder3/folder4/folder5"
            )
        )
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))

        findFirstChildByNameInList(tree, "folder4")?.let { folder4 ->
            assertTrue(
                actual = folder4.children.find { it.fileName == "folder5" && it.isFolder } != null,
                message = "folder4 should contain folder5",
            )
        } ?: fail("folder4 doesn't exist")
        findFirstChildByNameInList(tree, "folder5")?.let { folder5 ->
            assertTrue(
                actual = folder5.children.find { it.fileName == "file_in_folder1_folder2_folder3_folder4_folder5.txt" } != null,
                message = "file_in_folder1_folder2_folder3_folder4_folder5.txt does not exist in folder1/folder2/folder3/folder4/folder5",
            )
        } ?: fail("folder5 doesn't exist")
    }

    @Test
    fun fileVeryDeepInFoldersTree() {
        val filesList = listOf(
            FileDB(
                fileName = "hidden_file.txt",
                path = "folder1/folder2/folder3/folder4/folder5/folder6/folder7/folder8/folder9/folder10/folder11/folder12/folder13/folder14/folder15/folder16/folder17/folder18/folder19/folder20/folder21/folder22/folder23/folder24/folder25"
            )
        )
        tree.addAll(FileUtils.getFileDBTree("containerUUID", filesList))
        findFirstChildByNameInList(tree, "folder24")?.let { folder24 ->
            assertTrue(
                actual = folder24.children.find { it.fileName == "folder25" && it.isFolder } != null,
                message = "folder24 should contain folder25",
            )

        } ?: fail("folder24 doesn't exist")
        findFirstChildByNameInList(tree, "folder25")?.let { folder25 ->
            assertTrue(
                actual = folder25.children.find { it.fileName == "hidden_file.txt" } != null,
                message = "folder25 should contain hidden_file.txt",
            )
        } ?: fail("folder25 doesn't exist")
    }

    private fun List<FileDB>.getFileDB(name: String) = find { it.fileName == name }

    private fun findFirstChildByNameInList(tree: List<FileDB>, fileName: String): FileDB? {
        tree.forEach { file ->
            val foundChild = file.findChildByName(fileName)
            if (foundChild != null) {
                return foundChild
            }
        }
        return null
    }

    private fun FileDB.findChildByName(fileName: String): FileDB? {
        if (this.fileName == fileName) return this

        children.forEach { child ->
            child.findChildByName(fileName)?.let {
                return it
            }
        }

        return null
    }
}
