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

import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseProvider
import com.infomaniak.multiplatform_swisstransfer.database.getAppDatabase
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

class UploadTest : RobolectricTestsBase() {
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

    @Test
    fun canCreateAndGetUploadTransfer() = runTest {
        val transfer = createPendingUploadTransfer("upload1")
        appDatabase.getTransferDao().upsertTransfer(transfer)

        val result = appDatabase.getUploadDao().getTransfer("upload1")
        assertNotNull(result)
        assertEquals("upload1", result.id)
        assertEquals(TransferStatus.PENDING_UPLOAD, result.transferStatus)
    }

    @Test
    fun canGetAllUploadTransfers() = runTest {
        // Insert 2 pending upload transfers
        listOf("upload1", "upload2").forEach { id ->
            appDatabase.getTransferDao().upsertTransfer(createPendingUploadTransfer(id))
        }

        val uploads = appDatabase.getUploadDao().getAllUploadsTransfers()
        assertEquals(2, uploads.count())
    }

    @Test
    fun canGetLastUploadTransferFlow() = runTest {
        // Insert 2 pending uploads with different createdAt
        val upload1 = createPendingUploadTransfer("upload1", createdAt = 1000)
        val upload2 = createPendingUploadTransfer("upload2", createdAt = 2000)

        appDatabase.getTransferDao().upsertTransfer(upload1)
        appDatabase.getTransferDao().upsertTransfer(upload2)

        val lastUpload = appDatabase.getUploadDao().getLastUploadTransferFlow().first()
        assertNotNull(lastUpload)
        assertEquals("upload2", lastUpload.id) // Most recent
    }

    @Test
    fun canGetUploadsCount() = runTest {
        // Insert 2 pending uploads and 1 ready transfer
        appDatabase.getTransferDao().upsertTransfer(createPendingUploadTransfer("upload1"))
        appDatabase.getTransferDao().upsertTransfer(createPendingUploadTransfer("upload2"))
        appDatabase.getTransferDao().upsertTransfer(
            createTransferWithStatus("transfer1", TransferStatus.READY)
        )

        val count = appDatabase.getUploadDao().getUploadsCount()
        assertEquals(2, count)
    }

    @Test
    fun canRemoveUploadByChangingStatus() = runTest {
        val transfer = createPendingUploadTransfer("upload1")
        appDatabase.getTransferDao().upsertTransfer(transfer)

        // Verify it exists as upload
        assertEquals(1, appDatabase.getUploadDao().getUploadsCount())

        // "Remove" by updating status to READY (simulating upload completion)
        val completedTransfer = transfer.copy(transferStatus = TransferStatus.READY)
        appDatabase.getTransferDao().upsertTransfer(completedTransfer)

        // Should no longer appear as pending upload
        assertEquals(0, appDatabase.getUploadDao().getUploadsCount())
    }

    @Test
    fun onlyPendingUploadStatusReturnsUploads() = runTest {
        // Insert transfers with different statuses
        appDatabase.getTransferDao().upsertTransfer(createPendingUploadTransfer("upload1"))
        appDatabase.getTransferDao().upsertTransfer(createTransferWithStatus("ready1", TransferStatus.READY))
        appDatabase.getTransferDao().upsertTransfer(createTransferWithStatus("virus1", TransferStatus.WAIT_VIRUS_CHECK))

        val uploads = appDatabase.getUploadDao().getAllUploadsTransfers()
        assertEquals(1, uploads.count())
        assertEquals("upload1", uploads.first().id)
    }

    @Test
    fun getAllUploadsWithMixedDataReturnsOnlyUploads() = runTest {
        // Insert multiple uploads and completed transfers
        appDatabase.getTransferDao().upsertTransfer(createPendingUploadTransfer("upload1"))
        appDatabase.getTransferDao().upsertTransfer(createPendingUploadTransfer("upload2"))
        appDatabase.getTransferDao().upsertTransfer(createTransferWithStatus("ready1", TransferStatus.READY))
        appDatabase.getTransferDao().upsertTransfer(createTransferWithStatus("ready2", TransferStatus.READY))
        appDatabase.getTransferDao().upsertTransfer(createTransferWithStatus("expired", TransferStatus.EXPIRED_DATE))

        // UploadDao should only return pending uploads
        val uploads = appDatabase.getUploadDao().getAllUploadsTransfers()
        assertEquals(2, uploads.count())
        assertEquals(setOf("upload1", "upload2"), uploads.map { it.id }.toSet())
    }

    //region Helper methods
    private fun createPendingUploadTransfer(
        id: String,
        createdAt: Long = 0,
    ): TransferDB = TransferDB(
        transfer = DummyTransferForV2.transfer1,
        linkId = null,
        userOwnerId = userId,
    ).copy(
        id = id,
        createdAt = createdAt,
        transferDirection = TransferDirection.SENT,
        transferStatus = TransferStatus.PENDING_UPLOAD,
    )

    private fun createTransferWithStatus(
        id: String,
        status: TransferStatus,
    ): TransferDB = TransferDB(
        transfer = DummyTransferForV2.transfer1,
        linkId = null,
        userOwnerId = userId,
    ).copy(
        id = id,
        transferDirection = TransferDirection.SENT,
        transferStatus = status,
    )
    //endregion
}