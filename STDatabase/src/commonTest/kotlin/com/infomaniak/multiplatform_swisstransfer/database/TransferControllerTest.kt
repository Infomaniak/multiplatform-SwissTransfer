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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.dataset.DummyTransfer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class TransferControllerTest {

    private lateinit var realmProvider: RealmProvider
    private lateinit var transferController: TransferController

    @BeforeTest
    fun setup() {
        realmProvider = RealmProvider(loadDataInMemory = true).apply { openTransfersDb(userId = 0) }
        transferController = TransferController(realmProvider)
    }

    @AfterTest
    fun tearDown() = runTest {
        transferController.removeData()
        realmProvider.closeTransfersDb()
    }

    @Test
    fun canCreateSentTransfer() = runTest {
        canCreateTransfer(TransferDirection.SENT)
    }

    @Test
    fun canCreateReceivedTransfer() = runTest {
        canCreateTransfer(TransferDirection.RECEIVED)
    }

    @Test
    fun canGetTransfers() = runTest {
        addTwoRandomTransfersInDatabase()
        val transfers = transferController.getTransfers()
        assertEquals(2, transfers.count(), "The transfer list must contain 2 items")
    }

    @Test
    fun canGetSentTransfers() = runTest {
        canGetTransfersByDirection(TransferDirection.SENT)
    }

    @Test
    fun canGetReceivedTransfers() = runTest {
        canGetTransfersByDirection(TransferDirection.RECEIVED)
    }

    @Test
    fun canGetNotReadyTransfers() = runTest {
        addTwoRandomTransfersInDatabase()
        val transfers = transferController.getNotReadyTransfers()
        assertEquals(transfers.count(), 1)
        assertEquals(transfers.first().transferStatus, TransferStatus.WAIT_VIRUS_CHECK)
    }

    @Test
    fun canUpdateAnExistingTransfer() = runTest {
        // Insert a transfer
        val transfer1 = DummyTransfer.transfer1
        val password = "password"
        transferController.upsert(transfer1, TransferDirection.SENT, password)
        val realmTransfer1 = transferController.getTransfer(transfer1.linkUUID)
        assertNotNull(realmTransfer1)

        // Update the transfer
        val transfer2 = object : Transfer by transfer1 {
            override var containerUUID: String = "transfer2"
        }
        transferController.upsert(transfer2, TransferDirection.SENT, password)
        val realmTransfers = transferController.getTransfers()
        assertNotNull(realmTransfers)
        assertEquals(1, realmTransfers.count())
        assertEquals(transfer2.containerUUID, realmTransfers.first().containerUUID)
        assertEquals(password, realmTransfer1.password)
        assertEquals(realmTransfer1.password, realmTransfers.first().password)
    }

    @Test
    fun canDeleteExpiredTransfers() = runTest {

        transferController.upsert(DummyTransfer.expired, TransferDirection.SENT, password = null)
        transferController.upsert(DummyTransfer.notExpired, TransferDirection.RECEIVED, password = null)

        transferController.deleteExpiredTransfers()

        val transfers = transferController.getTransfers()
        assertEquals(transfers.count(), 1, "The transfers table should contain only 1 transfer")
        assertEquals(transfers.first().linkUUID, DummyTransfer.notExpired.linkUUID, "The remaining transfer should be `transfer2`")
    }

    @Test
    fun downloadManagerId() = runTest {
        val targetTransfer = DummyTransfer.expired
        /** Will be used to prove operations on `targetTransfer` don't affect other transfers. */
        val referenceTransfer = DummyTransfer.notExpired
        transferController.upsert(referenceTransfer, TransferDirection.RECEIVED, password = null)
        transferController.upsert(targetTransfer, TransferDirection.RECEIVED, password = null)

        val downloadManagerIds = object {
            private var lastId: Long = 0L
            val refFirstFile = ++lastId
            val refSecondFile = ++lastId
            val refZip = ++lastId
            val firstFile = ++lastId
            val secondFile = ++lastId
            val zip = ++lastId
        }
        run prepareReferenceTransfer@{
            val transferUUID = referenceTransfer.linkUUID
            val file1 = referenceTransfer.container!!.files[0]
            val file2 = referenceTransfer.container!!.files[1]
            transferController.writeDownloadManagerId(
                transferUUID = transferUUID,
                fileUid = file1.uuid,
                uniqueDownloadManagerId = downloadManagerIds.refFirstFile
            )
            transferController.writeDownloadManagerId(
                transferUUID = transferUUID,
                fileUid = file2.uuid,
                uniqueDownloadManagerId = downloadManagerIds.refSecondFile
            )
            transferController.writeDownloadManagerId(
                transferUUID = transferUUID,
                fileUid = null,
                uniqueDownloadManagerId = downloadManagerIds.refZip
            )
        }
        suspend fun readDlManagerId(transfer: Transfer, file: File?): Long? {
            return transferController.readDownloadManagerId(transferUUID = transfer.linkUUID, fileUid = file?.uuid)
        }

        suspend fun writeDlManagerId(transfer: Transfer, file: File?, uniqueId: Long?) {
            transferController.writeDownloadManagerId(
                transferUUID = transfer.linkUUID,
                fileUid = file?.uuid,
                uniqueDownloadManagerId = uniqueId
            )
        }

        run {
            val file1 = targetTransfer.container!!.files[0]
            val file2 = targetTransfer.container!!.files[1]
            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = downloadManagerIds.firstFile)
            writeDlManagerId(transfer = targetTransfer, file = file2, uniqueId = downloadManagerIds.secondFile)
            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = downloadManagerIds.zip)

            readDlManagerId(targetTransfer, file1) shouldBe downloadManagerIds.firstFile
            readDlManagerId(targetTransfer, file2) shouldBe downloadManagerIds.secondFile
            readDlManagerId(targetTransfer, file = null) shouldBe downloadManagerIds.zip

            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = null)
            readDlManagerId(targetTransfer, file1) shouldBe null
            readDlManagerId(targetTransfer, file2) shouldBe downloadManagerIds.secondFile
            readDlManagerId(targetTransfer, file = null) shouldBe downloadManagerIds.zip

            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = null)
            readDlManagerId(targetTransfer, file1) shouldBe null
            readDlManagerId(targetTransfer, file2) shouldBe downloadManagerIds.secondFile
            readDlManagerId(targetTransfer, file = null) shouldBe null

            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = downloadManagerIds.firstFile)
            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = downloadManagerIds.zip)
            transferController.deleteTransfer(targetTransfer.linkUUID)
            readDlManagerId(targetTransfer, file1) shouldBe null
            readDlManagerId(targetTransfer, file2) shouldBe null
            readDlManagerId(targetTransfer, file = null) shouldBe null

            transferController.upsert(targetTransfer, TransferDirection.RECEIVED, password = null)
            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = downloadManagerIds.firstFile)
            writeDlManagerId(transfer = targetTransfer, file = file2, uniqueId = downloadManagerIds.secondFile)
            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = downloadManagerIds.zip)
            transferController.deleteExpiredTransfers()
        }

        run checkReferenceWasUnchanged@{
            val file1 = referenceTransfer.container!!.files[0]
            val file2 = referenceTransfer.container!!.files[1]
            readDlManagerId(referenceTransfer, file1) shouldBe downloadManagerIds.refFirstFile
            readDlManagerId(referenceTransfer, file2) shouldBe downloadManagerIds.refSecondFile
            readDlManagerId(referenceTransfer, file = null) shouldBe downloadManagerIds.refZip
        }
    }

    private inline infix fun <T> T.shouldBe(expected: T) {
        assertEquals(expected = expected, actual = this)
    }

    @Test
    fun canRemoveTransfers() = runTest {
        transferController.upsert(DummyTransfer.transfer1, TransferDirection.SENT, password = null)
        transferController.removeData()
        assertEquals(0, transferController.getTransfers().count(), "The transfers table must be empty")
    }

    private suspend fun canCreateTransfer(sent: TransferDirection) {
        val transfer = DummyTransfer.transfer1
        transferController.upsert(transfer, sent, transfer.password)
        val realmTransfer = transferController.getTransfer(transfer.linkUUID)
        assertNotNull(realmTransfer, "The transfer cannot be null")
        assertEquals(sent, realmTransfer.transferDirection)
        assertEquals(transfer.container?.uuid, realmTransfer.container?.uuid, "The container is missing")
    }

    private suspend fun addTwoRandomTransfersInDatabase() {
        DummyTransfer.transfers.take(2).forEachIndexed { index, transfer ->
            val transferDirection = if (index == 0) TransferDirection.SENT else TransferDirection.RECEIVED
            transferController.upsert(transfer, transferDirection, transfer.password)
        }
    }

    private suspend fun canGetTransfersByDirection(transferDirection: TransferDirection) {
        addTwoRandomTransfersInDatabase()
        val transfers = transferController.getTransfers(transferDirection)
        assertNotNull(transfers)
        assertEquals(1, transfers.count(), "The transfer list must contain 1 item")
        assertEquals(transferDirection, transfers.first().transferDirection)
    }
}
