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
package com.infomaniak.multiplatform_swisstransfer.common

import com.infomaniak.multiplatform_swisstransfer.common.models.files.DisplayableFile
import com.infomaniak.multiplatform_swisstransfer.common.models.files.UploadFile
import com.infomaniak.multiplatform_swisstransfer.common.utils.FileUtils
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FileUtilsTest {

    private val tree = mutableListOf<DisplayableFile>()

    @BeforeTest
    fun setup() {
        tree.clear()
    }

    @Test
    fun basicTest() {
        val textFile1 = DisplayableFile(
            id = Uuid.random().toString(),
            name = "file1.txt",
            isFolder = false,
            children = mutableListOf(),
            parent = null,
            url = "/file1.txt",
            size = 10L,
        )
        val folder1 = DisplayableFile(
            id = Uuid.random().toString(),
            name = "folder1",
            isFolder = true,
            children = mutableListOf(),
            parent = null,
            url = "/folder1",
            size = 10L,
        )
        val folder = FileUtils.findFolder(listOf("folder1", "folder2"), listOf(textFile1, folder1))
        println(folder)
    }

    @Test
    fun filesInRoot() {
        val filesList = listOf(
            UploadFile(url = "file1.txt", path = ""),
            UploadFile(url = "file2.txt", path = ""),
        )
        tree.addAll(FileUtils.prepareForDisplay(filesList))

        // Files at the root of the tree
        tree.getDisplayableFile("file1.txt")?.let { file1 ->
            assertTrue(file1.children.isEmpty())
            assertTrue(!file1.isFolder)
        }
        tree.getDisplayableFile("file2.txt")?.let { file2 ->
            assertTrue(file2.children.isEmpty())
            assertTrue(!file2.isFolder)
        }
    }

    @Test
    fun filesInFolder() {
        val filesList = listOf(
            UploadFile(url = "file3.txt", path = "folder1"),
            UploadFile(url = "file4.txt", path = "folder1"),
            UploadFile(url = "empty_file.txt", path = "folder1/randomFolder"),
            UploadFile(url = "file5.txt", path = "folder2"),
            UploadFile(url = "file6.txt", path = "folder2"),
        )
        tree.addAll(FileUtils.prepareForDisplay(filesList))

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
                actual = innerFiles.find { it.name == "file3.txt" } != null,
                message = "Children of folder1 should contain two files",
            )
            assertTrue(
                actual = innerFiles.find { it.name == "file4.txt" } != null,
                message = "Children of folder1 should contain two files",
            )
        } ?: fail("folder1 doesn't exist")


        // Files in folder2
        findFirstChildByNameInList(tree, "folder2")?.let { folder2 ->
            assertTrue(
                actual = folder2.children.none { it.isFolder },
                message = "Children of folder2 should contain one folder",
            )
            assertTrue(
                actual = folder2.children.filter { !it.isFolder }.size == 2,
                message = "Children of folder2 should contain two files",
            )
            val innerFiles = folder2.children.filter { !it.isFolder }
            assertTrue(
                actual = innerFiles.find { it.name == "file5.txt" } != null,
                message = "file4.txt in folder2 does not exist",
            )
            assertTrue(
                actual = innerFiles.find { it.name == "file6.txt" } != null,
                message = "file5.txt in folder2 does not exist",
            )
        } ?: fail("folder2 doesn't exist")
    }

    @Test
    fun fileContainedIn2NestedFolders() {
        val filesList = listOf(UploadFile(url = "file_in_folder1_folder2.txt", path = "folder1/folder2"))
        tree.addAll(FileUtils.prepareForDisplay(filesList))

        // File contained in one folder contained in another folder
        findFirstChildByNameInList(tree, "folder2")?.let { folder2InFolder1 ->
            assertTrue(
                actual = folder2InFolder1.children.find { it.name == "file_in_folder1_folder2.txt" } != null,
                message = "file_in_folder1_folder2.txt does not exist in folder1/folder2",
            )
        } ?: fail("folder2 in folder1 doesn't exist")
    }

    @Test
    fun fileContainedIn5NestedFolders() {
        val filesList = listOf(
            UploadFile(
                url = "file_in_folder1_folder2_folder3_folder4_folder5.txt",
                path = "folder1/folder2/folder3/folder4/folder5"
            )
        )
        tree.addAll(FileUtils.prepareForDisplay(filesList))

        findFirstChildByNameInList(tree, "folder5")?.let { folder5 ->
            assertTrue(
                actual = folder5.parent != null,
                message = "file_in_folder1_folder2.txt does not exist in folder1/folder2",
            )
            assertTrue(
                actual = folder5.parent!!.name == "folder4",
                message = "file_in_folder1_folder2.txt does not exist in folder1/folder2",
            )
            assertTrue(
                actual = folder5.children.find { it.name == "file_in_folder1_folder2_folder3_folder4_folder5.txt" } != null,
                message = "file_in_folder1_folder2_folder3_folder4_folder5.txt does not exist in folder1/folder2/folder3/folder4/folder5",
            )
        } ?: fail("folder5 doesn't exist")
    }

    private fun List<DisplayableFile>.getDisplayableFile(name: String) = find { it.name == name }

    private fun findFirstChildByNameInList(tree: List<DisplayableFile>, targetName: String): DisplayableFile? {
        tree.forEach { file ->
            val foundChild = file.findChildByName(targetName)
            if (foundChild != null) {
                return foundChild
            }
        }
        return null
    }
}
