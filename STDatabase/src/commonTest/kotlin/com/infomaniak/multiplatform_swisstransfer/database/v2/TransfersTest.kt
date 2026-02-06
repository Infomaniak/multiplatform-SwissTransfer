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

class TransfersTest : RobolectricTestsBase() {

    private lateinit var appDatabase: AppDatabase
    private val userId = 0L

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
        val transfers = appDatabase.getTransferDao().getTransfers(userId).first()
        assertEquals(2, transfers.count(), "The transfers list must contain 2 items")
    }

    @Test
    fun getTransfersExcludesPendingUploads() = runTest {
        // Insert a completed transfer
        insertTransfer(DummyTransferForV2.transfer1, TransferDirection.SENT, null)
        // Insert a pending upload
        val uploadTransfer = TransferDB(
            transfer = DummyTransferForV2.transfer2,
            linkId = null,
            userOwnerId = userId,
        ).copy(
            id = "upload1",
            transferDirection = TransferDirection.SENT,
            transferStatus = TransferStatus.PENDING_UPLOAD,
        )
        appDatabase.getTransferDao().upsertTransfer(uploadTransfer)

        val transfers = appDatabase.getTransferDao().getTransfers(userId).first()
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

        val validTransfers = appDatabase.getTransferDao().getValidTransfers(userId, transferDirection).first()

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

        val expiredTransfers = appDatabase.getTransferDao().getExpiredTransfers(userId, transferDirection).first()

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
        val count = appDatabase.getTransferDao().getTransfersCount(userId, TransferDirection.SENT).first()
        assertEquals(1, count, "The transfers count must be 1")
    }

    @Test
    fun canGetTransferFlow() = runTest {
        val transfer = DummyTransferForV2.transfer1
        insertTransfer(transfer, TransferDirection.SENT, null)
        val result = appDatabase.getTransferDao().getTransfer(userId, transfer.id).first()
        assertNotNull(result)
        assertEquals(
            expected = transfer.id,
            actual = result.id,
            message = "The transfer should be `transfer1`",
        )
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
        val result = appDatabase.getTransferDao().getTransfer(userId, transfer.id).first()
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
        appDatabase.getTransferDao().upsertTransfer(updatedTransfer)

        val result = appDatabase.getTransferDao().getTransfer(userId, transfer.id).first()
        assertNotNull(result)
        assertEquals("Updated Title", result.title)
    }
    //endregion

    //region Delete data
    @Test
    fun canDeleteExpiredTransfers() = runTest {
        insertTransfer(DummyTransferForV2.expired, TransferDirection.SENT, null)
        insertTransfer(DummyTransferForV2.notExpired, TransferDirection.RECEIVED, null)

        appDatabase.getTransferDao().deleteExpiredTransfers()

        val transfers = appDatabase.getTransferDao().getTransfers(userId).first()
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
        appDatabase.getTransferDao().deleteTransfers(userId)
        val transfers = appDatabase.getTransferDao().getTransfers(userId).first()
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
        appDatabase.getTransferDao().upsertFile(file)

        // Verify file exists
        val filesBefore = appDatabase.getTransferDao().getTransferRootFiles(transfer.id)
        assertEquals(1, filesBefore.count(), "Should have 1 file before deletion")

        // Delete the transfer
        val transferToDelete = appDatabase.getTransferDao().getTransfer(userId, transfer.id).first()
        assertNotNull(transferToDelete)
        appDatabase.getTransferDao().deleteTransfer(transferToDelete)

        // Verify transfer and files are deleted
        val transfers = appDatabase.getTransferDao().getTransfers(userId).first()
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
        appDatabase.getTransferDao().upsertFile(FileDB(file = file, transferId = transfer.id, folderId = null))

        val result = appDatabase.getTransferDao().getFile("file1").first()
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

        appDatabase.getTransferDao().upsertFile(rootFile)
        appDatabase.getTransferDao().upsertFile(folderFile1)
        appDatabase.getTransferDao().upsertFile(folderFile2)

        // Test getTransferRootFiles
        val rootFiles = appDatabase.getTransferDao().getTransferRootFiles(transfer.id)
        assertEquals(1, rootFiles.count())
        assertEquals(rootFile.id, rootFiles.first().id)

        // Test getTransferFolderFiles
        val folderFiles = appDatabase.getTransferDao().getTransferFolderFiles(transfer.id, "folder1").first()
        val folderFilesFlow = appDatabase.getTransferDao().getFilesByFolderId("folder1")
        assertEquals(2, folderFiles.count())
        assertEquals(2, folderFilesFlow.first().count())
    }
    //endregion

    //region Edge cases
    @Test
    fun getTransferWithNonExistentIdReturnsNull() = runTest {
        val result = appDatabase.getTransferDao().getTransfer(userId, "non-existent-id").first()
        assertNull(result)
    }

    @Test
    fun getFileWithNonExistentIdReturnsNull() = runTest {
        val result = appDatabase.getTransferDao().getFile("non-existent-id").first()
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
        appDatabase.getTransferDao().upsertFile(file)

        // Update same file (same ID) with new path
        val updatedFile = FileDB(
            file = DummyTransferForV2.createDummyFile(path = "new/path.txt", mimeType = "text/plain", id = "file1"),
            transferId = transfer.id,
            folderId = null,
        )
        appDatabase.getTransferDao().upsertFile(updatedFile)

        val result = appDatabase.getTransferDao().getFile("file1").first()
        assertNotNull(result)
        assertEquals("new/path.txt", result.path)
        assertEquals(10_000_000L, result.size)
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
        )
        appDatabase.getTransferDao().upsertTransfer(transferDB)
    }
    //endregion
}
