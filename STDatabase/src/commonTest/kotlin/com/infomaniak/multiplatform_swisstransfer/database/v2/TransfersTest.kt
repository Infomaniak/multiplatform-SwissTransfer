/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer.database.v2

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseProvider
import com.infomaniak.multiplatform_swisstransfer.database.getAppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.v2.dataset.DummyTransferForV2
import com.infomaniak.multiplatform_swisstransfer.database.v2.utils.DatabaseConfigFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TransfersTest : RobolectricTestsBase() {

    private lateinit var appDatabase: AppDatabase
    private val userId = 0L
    private val transferDao get() = appDatabase.getTransferDao()

    @BeforeTest
    fun createDb() {
        val databaseProvider = DatabaseProvider(DatabaseConfigFactory.createTestDatabaseConfig())
        appDatabase = databaseProvider.getAppDatabase(inMemory = true, driver = DatabaseConfigFactory.driver())
    }

    @AfterTest
    fun closeDb() {
        appDatabase.close()
    }

    //region Get data
    @Test
    fun canGetAllTransfers() = runTest {
        addTwoRandomTransfersInDatabase()
        val transfers = transferDao.transfersFlow(userId).first()
        assertEquals(2, transfers.count(), "The transfers list must contain 2 items")
    }

    @Test
    fun getTransfersExcludesPendingUploads() = runTest {
        // Insert a completed transfer
        insertTransfer(DummyTransferForV2.transfer1, TransferDirection.SENT, null)

        // Insert a pending upload
        val uploadTransfer = TransferDB(
            transfer = DummyTransferForV2.transfer2,
            direction = TransferDirection.SENT,
            linkId = null,
            userOwnerId = userId,
        ).copy(
            id = "upload1",
            transferDirection = TransferDirection.SENT,
            transferStatus = TransferStatus.PENDING_UPLOAD,
        )
        transferDao.upsertTransfer(uploadTransfer)

        val transfers = transferDao.transfersFlow(userId).first()
        // Should only return the completed transfer, not the pending upload
        assertEquals(1, transfers.count())
        assertEquals(DummyTransferForV2.transfer1.id, transfers.first().id)
    }

    @Test
    fun canGetValidTransfers() = runTest {
        addTwoRandomTransfersInDatabase()

        val transferDirection = TransferDirection.SENT
        insertTransfer(DummyTransferForV2.expired, transferDirection, null)
        insertTransfer(DummyTransferForV2.notExpired, transferDirection, null)

        val validTransfers = transferDao.validTransfersFlow(userId, transferDirection).first()

        assertEquals(1, validTransfers.count(), "The valid transfers list must contain 1 item")
        assertEquals(
            expected = DummyTransferForV2.notExpired.id,
            actual = validTransfers.first().id,
            message = "The transfer in `validTransfers` should be `notExpired`",
        )
    }

    @Test
    fun canGetExpiredTransfers() = runTest {
        addTwoRandomTransfersInDatabase()

        val transferDirection = TransferDirection.SENT
        insertTransfer(DummyTransferForV2.expired, transferDirection, null)
        insertTransfer(DummyTransferForV2.notExpired, transferDirection, null)

        val expiredTransfers = transferDao.expiredTransfersFlow(userId, transferDirection).first()

        assertEquals(1, expiredTransfers.count(), "The expired transfers list must contain 1 item")
        assertEquals(
            expected = DummyTransferForV2.expired.id,
            actual = expiredTransfers.first().id,
            message = "The transfer in `expiredTransfers` should be `expired`",
        )
    }

    @Test
    fun canGetTransfersCountFlow() = runTest {
        addTwoRandomTransfersInDatabase()
        val count = transferDao.transfersCountFlow(userId, TransferDirection.SENT).first()
        assertEquals(1, count, "The transfers count must be 1")
    }

    @Test
    fun canGetTransferFlow() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)
        val result = transferDao.transferFlow(userId, transfer.id).first()
        assertNotNull(result)
        assertEquals(
            expected = transfer.id,
            actual = result.id,
            message = "The transfer should be `transfer1`",
        )
    }

    @Test
    fun canGetTransferByLinkId() = runTest {
        val transfer1 = DummyTransferForV2.transfer1
        val transfer2 = DummyTransferForV2.transfer2
        val expectedLinkId = "linkId"

        insertTransfer(transfer1, TransferDirection.RECEIVED, password = null, linkId = expectedLinkId)
        insertTransfer(transfer2, TransferDirection.RECEIVED, password = null)

        val transferDBResult = transferDao.getTransferByLinkId(expectedLinkId)
        assertNotNull(transferDBResult)
        assertEquals(expectedLinkId, transferDBResult.linkId)
    }

    @Test
    fun getTransferByLinkId_returnsNull_whenLinkIdDoesNotMatch() = runTest {
        val transfer = DummyTransferForV2.transfer1

        insertTransfer(transfer, TransferDirection.RECEIVED, password = null)

        val transferDBResult = transferDao.getTransferByLinkId("linkId")
        assertNull(transferDBResult)
    }
    //endregion

    //region Upsert data
    @Test
    fun canCreateSentTransfer() = runTest {
        canCreateTransfer(TransferDirection.SENT)
    }

    @Test
    fun canCreateReceivedTransfer() = runTest {
        canCreateTransfer(TransferDirection.RECEIVED)
    }

    private suspend fun canCreateTransfer(direction: TransferDirection) {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, direction, "password123")
        val result = transferDao.transferFlow(userId, transfer.id).first()
        assertNotNull(result, "The transfer cannot be null")
        assertEquals(direction, result.transferDirection)
    }
    //endregion

    //region Update data
    @Test
    fun canUpsertTransfer() = runTest {
        // Insert a transfer
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        // Update the transfer with new values
        val updatedTransfer = TransferDB(
            id = transfer.id,
            senderEmail = transfer.senderEmail,
            title = "Updated Title",
            message = transfer.message,
            createdAt = transfer.createdAt,
            expiresAt = transfer.expiresAt,
            totalSize = transfer.totalSize,
            userOwnerId = userId,
            transferDirection = TransferDirection.SENT,
            transferStatus = TransferStatus.READY,
        )
        transferDao.upsertTransfer(updatedTransfer)

        val result = transferDao.transferFlow(userId, transfer.id).first()
        assertNotNull(result)
        assertEquals("Updated Title", result.title)
    }
    //endregion

    //region Delete data
    @Test
    fun canDeleteExpiredTransfers() = runTest {
        insertTransfer(DummyTransferForV2.expired, TransferDirection.SENT, null)
        insertTransfer(DummyTransferForV2.notExpired, TransferDirection.RECEIVED, null)

        transferDao.deleteExpiredTransfers()

        val transfers = transferDao.transfersFlow(userId).first()
        assertEquals(
            expected = 1,
            actual = transfers.count(),
            message = "The transfers table should contain only 1 transfer",
        )
        assertEquals(
            expected = DummyTransferForV2.notExpired.id,
            actual = transfers.first().id,
            message = "The remaining transfer should be `notExpired`",
        )
    }

    @Test
    fun canRemoveAllTransfers() = runTest {
        insertTransfer(DummyTransferForV2.transfer1, TransferDirection.SENT, null)
        transferDao.deleteTransfers(userId)
        val transfers = transferDao.transfersFlow(userId).first()
        assertEquals(0, transfers.count(), "The transfers table must be empty")
    }

    @Test
    fun canDeleteTransferWithFiles() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        // Add a file to the transfer
        val file = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "path/to/file", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = null,
        )
        transferDao.upsertFile(file)

        // Verify file exists
        val filesBefore = transferDao.getTransferRootFiles(transfer.id)
        assertEquals(1, filesBefore.count(), "Should have 1 file before deletion")

        // Delete the transfer
        val transferToDelete = transferDao.transferFlow(userId, transfer.id).first()
        assertNotNull(transferToDelete)
        transferDao.deleteTransfer(transferToDelete)

        // Verify transfer and files are deleted
        val transfers = transferDao.transfersFlow(userId).first()
        assertEquals(0, transfers.count(), "The transfers table must be empty")
    }
    //endregion

    //region File operations
    @Test
    fun canInsertAndGetFile() = runTest {
        // Insert transfer first (needed for foreign key)
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        val file = DummyTransferForV2.createDummyFile(path = "path/to/file.txt", mimeType = "text/plain", id = "file1")
        transferDao.upsertFile(FileDB(file = file, transferId = transfer.id, folderId = null))

        val result = transferDao.fileFlow("file1").first()
        assertNotNull(result)
        assertEquals("file1", result.id)
        assertEquals("path/to/file.txt", result.path)
    }

    @Test
    fun canGetFilesInFolder() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        // Root files (folderId = null)
        val rootFile = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "root.txt", mimeType = "text/plain"),
            transferId = transfer.id,
            folderId = null,
        )

        // Files in folder
        val folderFile1 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "folder/file1.txt", mimeType = "text/plain"),
            transferId = transfer.id,
            folderId = "folder1",
        )
        val folderFile2 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "folder/file2.txt", mimeType = "text/plain"),
            transferId = transfer.id,
            folderId = "folder1",
        )

        transferDao.upsertFile(rootFile)
        transferDao.upsertFile(folderFile1)
        transferDao.upsertFile(folderFile2)

        // Test getTransferRootFiles (single transfer)
        val rootFiles = transferDao.getTransferRootFiles(transfer.id)
        assertEquals(1, rootFiles.count())
        assertEquals(rootFile.id, rootFiles.first().id)

        // Test getTransferRootFiles with list of transferIds returns Map grouped by transferId
        val transfer2 = DummyTransferForV2.transfer2
        insertTransfer(transfer2, TransferDirection.SENT, null)

        val rootFile2 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "root2.txt", mimeType = "text/plain"),
            transferId = transfer2.id,
            folderId = null,
        )
        transferDao.upsertFile(rootFile2)

        val filesByTransfer = transferDao.getTransferRootFiles(listOf(transfer.id, transfer2.id))

        assertEquals(2, filesByTransfer.size, "Map should contain entries for both transfers")
        assertEquals(1, filesByTransfer[transfer.id]?.size, "First transfer should have 1 root file")
        assertEquals(1, filesByTransfer[transfer2.id]?.size, "Second transfer should have 1 root file")
        assertEquals(rootFile.id, filesByTransfer[transfer.id]?.first()?.id)
        assertEquals(rootFile2.id, filesByTransfer[transfer2.id]?.first()?.id)

        // Test getTransferFolderFiles
        val folderFiles = transferDao.transferFolderFilesFlow(transfer.id, "folder1").first()
        val folderFilesFlow = transferDao.filesByFolderIdFlow("folder1")
        assertEquals(2, folderFiles.count())
        assertEquals(2, folderFilesFlow.first().count())
    }
    //endregion

    //region Edge cases
    @Test
    fun getTransferWithNonExistentIdReturnsNull() = runTest {
        val result = transferDao.transferFlow(userId, "non-existent-id").first()
        assertNull(result)
    }

    @Test
    fun getFileWithNonExistentIdReturnsNull() = runTest {
        val result = transferDao.fileFlow("non-existent-id").first()
        assertNull(result)
    }

    @Test
    fun canUpdateExistingFile() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        val file = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "old/path.txt", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = null,
        )
        transferDao.upsertFile(file)

        // Update same file (same ID) with new path
        val updatedFile = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "new/path.txt", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = null,
        )
        transferDao.upsertFile(updatedFile)

        val result = transferDao.fileFlow("file1").first()
        assertNotNull(result)
        assertEquals("new/path.txt", result.path)
        assertEquals(10_000_000L, result.size)
    }

    @Test
    fun canGetTransferFilesOnly() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        val rootFile = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "root.txt", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = null,
        )
        val folderFile1 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "folder/file1.txt", mimeType = "text/plain", id = "file2"),
            transferId = transfer.id,
            folderId = "folder1",
        )
        val folderFile2 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "folder/subfolder/file2.txt", mimeType = "text/plain", id = "file3"),
            transferId = transfer.id,
            folderId = "subfolder1",
        )

        // SubFolder should not be returned
        val subfolder = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "folder/subfolder", mimeType = null, id = "subfolder"),
            transferId = transfer.id,
            folderId = "subfolder1",
        ).copy(isFolder = true)

        transferDao.upsertFile(rootFile)
        transferDao.upsertFile(folderFile1)
        transferDao.upsertFile(folderFile2)
        transferDao.upsertFile(subfolder)

        val allFiles = transferDao.getTransferFilesOnly(transfer.id)

        assertEquals(3, allFiles.size, "Should return all files in the transfer")
        assertTrue(allFiles.map { it.id }.containsAll(listOf("file1", "file2", "file3")))
    }

    @Test
    fun canGetFilesUnderPath() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        // Files in target folder "documents"
        val docFile1 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "documents/file1.txt", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = "folder1",
        )
        val docFile2 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "documents/file2.txt", mimeType = "text/plain", id = "file2"),
            transferId = transfer.id,
            folderId = "folder1",
        )

        // File in subfolder
        val subFile = FileDB(
            file = DummyTransferForV2.createDummyFile(
                path = "documents/subfolder/photo.jpg",
                mimeType = "image/jpeg",
                id = "file3"
            ),
            transferId = transfer.id,
            folderId = "subfolder1",
        )

        // File outside target folder (should not be returned)
        val otherFile = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "documentsother/file.txt", mimeType = "text/plain", id = "file4"),
            transferId = transfer.id,
            folderId = "otherFolder",
        )

        // File with % in name - tests protection against SQL LIKE wildcard injection
        val fileWithWildcardPercent = FileDB(
            file = DummyTransferForV2.createDummyFile(
                path = "documents/100%_complete.txt",
                mimeType = "text/plain",
                id = "file5"
            ),
            transferId = transfer.id,
            folderId = "folder1",
        )

        // File with _ in name - tests protection against SQL LIKE wildcard injection
        val fileWithWildcardUnderscore = FileDB(
            file = DummyTransferForV2.createDummyFile(
                path = "documents/file_name_v1.txt",
                mimeType = "text/plain",
                id = "file6"
            ),
            transferId = transfer.id,
            folderId = "folder1",
        )

        // Folder itself (should not be returned - NOT isFolder)
        val folderItself = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "documents", mimeType = null, id = "folder1"),
            transferId = transfer.id,
            folderId = null,
        ).copy(isFolder = true)

        transferDao.upsertFile(docFile1)
        transferDao.upsertFile(docFile2)
        transferDao.upsertFile(subFile)
        transferDao.upsertFile(otherFile)
        transferDao.upsertFile(fileWithWildcardPercent)
        transferDao.upsertFile(fileWithWildcardUnderscore)
        transferDao.upsertFile(folderItself)

        val folderFiles = transferDao.getFilesUnderPath(transfer.id, "documents")

        assertEquals(5, folderFiles.size, "Should return files in folder and subfolders")
        assertTrue(folderFiles.map { it.id }.containsAll(listOf("file1", "file2", "file3", "file5", "file6")))
        assertTrue(folderFiles.none { it.id == "file4" }, "Should not include files outside the folder path")
        assertTrue(folderFiles.none { it.isFolder }, "Should not include folders")
        assertTrue(folderFiles.any { it.path.contains("%") }, "Should correctly handle files with % in name")
        assertTrue(folderFiles.any { it.path.contains("_") }, "Should correctly handle files with _ in name")
    }

    @Test
    fun testGetFilesUnderPathWithWildcard() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        val docFile1 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "documents%/file1.txt", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = "folder1",
        )
        val otherFile = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "documentsother/file.txt", mimeType = "text/plain", id = "file4"),
            transferId = transfer.id,
            folderId = "otherFolder",
        )

        transferDao.upsertFile(docFile1)
        transferDao.upsertFile(otherFile)

        // With LIKE 'documents%', this would also match "documentsother/file.txt"
        // But with lexicographical range, % is treated as a literal character, so only the exact "documents%" prefix matches
        val folderFiles = transferDao.getFilesUnderPath(transfer.id, "documents%")

        assertEquals(1, folderFiles.size, "Should return only 1 file")
        assertTrue(folderFiles.any { it.id == "file1" }, "The % should be treated as a literal character, not a wildcard")
    }

    @Test
    fun testGetFilesUnderPathWithEmoji() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)

        val docFile1 = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "documents/😅test.txt", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = "folder1",
        )

        transferDao.upsertFile(docFile1)

        val folderFiles = transferDao.getFilesUnderPath(transfer.id, "documents")

        assertEquals(1, folderFiles.size, "Should return only 1 file")
        assertTrue(folderFiles.any { it.id == "file1" }, "The % should be treated as a literal character, not a wildcard")
    }
    //endregion

    //region Helper methods
    private suspend fun addTwoRandomTransfersInDatabase() {
        DummyTransferForV2.transfers.take(2).forEachIndexed { index, transfer ->
            println("index:$index id:${transfer.id}, status:${transfer.transferStatus}")
            val transferDirection = if (index == 0) TransferDirection.SENT else TransferDirection.RECEIVED
            insertTransfer(transfer, transferDirection, null)
        }
    }

    private suspend fun insertTransfer(
        transfer: Transfer,
        transferDirection: TransferDirection,
        password: String?,
        linkId: String? = null,
    ) {
        val transferDB = TransferDB(
            id = transfer.id,
            senderEmail = transfer.senderEmail,
            title = transfer.title,
            message = transfer.message,
            createdAt = transfer.createdAt,
            expiresAt = transfer.expiresAt,
            totalSize = transfer.totalSize,
            password = password,
            transferDirection = transferDirection,
            transferStatus = transfer.transferStatus,
            recipientsEmails = transfer.recipientsEmails,
            userOwnerId = userId,
            linkId = linkId,
        )
        transferDao.upsertTransfer(transferDB)
    }
    //endregion
}
