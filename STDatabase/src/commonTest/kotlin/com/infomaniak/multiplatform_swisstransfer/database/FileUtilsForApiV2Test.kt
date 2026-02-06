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

import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtilsForApiV2
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.File as FileInterfaceV2

class FileUtilsForApiV2Test {

    private val treeApiV2 = mutableListOf<FileDB>()

    private data class FileV2Mock(
        override val id: String,
        override val path: String,
        override val size: Long,
        override val mimeType: String? = null
    ) : FileInterfaceV2

    @AfterTest
    fun clean() {
        treeApiV2.clear()
    }

    @Test
    fun filesInRoot() {
        val filesList = listOf(
            FileV2Mock(id = "1", path = "file1.txt", size = 100L),
            FileV2Mock(id = "2", path = "file2.txt", size = 200L),
        )
        treeApiV2.addAll(FileUtilsForApiV2.getFileDbTree("transfer1", filesList))

        val rootFiles = treeApiV2.filter { !it.isFolder }
        assertEquals(2, rootFiles.size, "Should have 2 root files")
        assertNotNull(rootFiles.find { it.path == "file1.txt" }, "file1.txt should exist")
        assertNotNull(rootFiles.find { it.path == "file2.txt" }, "file2.txt should exist")
    }

    @Test
    fun filesInFolder() {
        val filesList = listOf(
            FileV2Mock(id = "1", path = "folder1/file3.txt", size = 100L),
            FileV2Mock(id = "2", path = "folder1/file4.txt", size = 200L),
            FileV2Mock(id = "3", path = "folder1/randomFolder/empty_file.txt", size = 50L),
            FileV2Mock(id = "4", path = "folder2/file5.txt", size = 300L),
            FileV2Mock(id = "5", path = "folder2/file6.txt", size = 400L),
        )
        treeApiV2.addAll(FileUtilsForApiV2.getFileDbTree("transfer2", filesList))

        val folder1 = treeApiV2.find { it.path == "folder1" && it.isFolder }
        val folder2 = treeApiV2.find { it.path == "folder2" && it.isFolder }
        val randomFolder = treeApiV2.find { it.path == "folder1/randomFolder" && it.isFolder }

        assertNotNull(folder1, "folder1 should exist")
        assertNotNull(folder2, "folder2 should exist")
        assertNotNull(randomFolder, "folder1/randomFolder should exist")

        assertEquals(350L, folder1.size, "folder1 size should be 350 (100 + 200 + 50)")
        assertEquals(700L, folder2.size, "folder2 size should be 700 (300 + 400)")
        assertEquals(50L, randomFolder.size, "randomFolder size should be 50")

        val folder1Children = treeApiV2.filter { it.folderId == folder1.id && !it.isFolder }
        assertEquals(2, folder1Children.size, "folder1 should have 2 direct file children")
    }

    @Test
    fun fileContainedIn5NestedFolders() {
        val path = "folder1/folder2/folder3/folder4/folder5"
        val filesList = listOf(
            FileV2Mock(id = "1", path = "$path/nested_file.txt", size = 1000L)
        )
        treeApiV2.addAll(FileUtilsForApiV2.getFileDbTree("transfer3", filesList))

        checkFolders("folder1/folder2/folder3/folder4/folder5", size = 1000L)

        val file = treeApiV2.find { it.path == "$path/nested_file.txt" }
        assertNotNull(file, "nested_file.txt should exist")
    }

    @Test
    fun folderSizeAccumulatedCorrectly() {
        val filesList = listOf(
            FileV2Mock(id = "1", path = "docs/report.pdf", size = 500L),
            FileV2Mock(id = "2", path = "docs/notes.txt", size = 300L),
            FileV2Mock(id = "3", path = "docs/subfolder/image.png", size = 200L),
        )
        treeApiV2.addAll(FileUtilsForApiV2.getFileDbTree("transfer4", filesList))

        val docs = treeApiV2.find { it.path == "docs" && it.isFolder }
        val subfolder = treeApiV2.find { it.path == "docs/subfolder" && it.isFolder }

        assertEquals(1000L, docs?.size, "docs size should be 1000 (500 + 300 + 200)")
        assertEquals(200L, subfolder?.size, "subfolder size should be 200")
    }

    @Test
    fun folderHierarchyIsCorrect() {
        val filesList = listOf(
            FileV2Mock(id = "1", path = "a/b/c/file.txt", size = 100L),
        )
        treeApiV2.addAll(FileUtilsForApiV2.getFileDbTree("transfer5", filesList))

        val folderA = treeApiV2.find { it.path == "a" && it.isFolder }
        val folderB = treeApiV2.find { it.path == "a/b" && it.isFolder }
        val folderC = treeApiV2.find { it.path == "a/b/c" && it.isFolder }

        assertNotNull(folderA, "folder 'a' should exist")
        assertNotNull(folderB, "folder 'a/b' should exist")
        assertNotNull(folderC, "folder 'a/b/c' should exist")

        assertEquals(null, folderA.folderId, "folder 'a' should have no parent")
        assertEquals(folderA.id, folderB.folderId, "folder 'a/b' should have 'a' as parent")
        assertEquals(folderB.id, folderC.folderId, "folder 'a/b/c' should have 'a/b' as parent")
    }

    private fun checkFolders(path: String, size: Long) {
        val parts = path.split("/")
        for (i in parts.indices) {
            val partialPath = parts.take(i + 1).joinToString("/")
            val folder = treeApiV2.find { it.path == partialPath && it.isFolder }
            assertNotNull(folder, "$partialPath should exist as folder")
            assertEquals(size, folder.size, "$partialPath should have correct accumulated size")
        }
    }
}
