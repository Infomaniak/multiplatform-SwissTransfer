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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtils
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

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
        tree.addAll(FileUtils.getFileDBTree(filesList))

        // Files at the root of the tree
        tree.getFileDB("file1.txt")?.let { file1 ->
            assertTrue(file1.children.isEmpty())
            assertTrue(!file1.isFolder)
        }
        tree.getFileDB("file2.txt")?.let { file2 ->
            assertTrue(file2.children.isEmpty())
            assertTrue(!file2.isFolder)
        }
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
        tree.addAll(FileUtils.getFileDBTree(filesList))

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
        tree.addAll(FileUtils.getFileDBTree(filesList))

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
        tree.addAll(FileUtils.getFileDBTree(filesList))

        findFirstChildByNameInList(tree, "folder5")?.let { folder5 ->
            assertTrue(
                actual = folder5.parent != null,
                message = "Parent of folder5 should not be null",
            )
            assertTrue(
                actual = folder5.parent!!.fileName == "folder4",
                message = "The parent of folder5 should be named folder4",
            )
            assertTrue(
                actual = folder5.children.find { it.fileName == "file_in_folder1_folder2_folder3_folder4_folder5.txt" } != null,
                message = "file_in_folder1_folder2_folder3_folder4_folder5.txt does not exist in folder1/folder2/folder3/folder4/folder5",
            )
        } ?: fail("folder5 doesn't exist")
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

    /**
     * Recursively searches for a child with the given name.
     *
     * @param fileName The name of the child to search for.
     * @return The [File] object representing the found child, or null if not found.
     */
    fun FileDB.findChildByName(fileName: String): FileDB? {
        if (this.fileName == fileName) return this

        children.forEach { child ->
            child.findChildByName(fileName)?.let {
                return it
            }
        }

        return null
    }
}
