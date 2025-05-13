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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.dataset.DummyTransfer
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.ContainerDB
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class TransferControllerTest {

    private lateinit var realmProvider: RealmProvider
    private lateinit var transferController: TransferController

    //region Configure tests
    @BeforeTest
    fun setup() = runTest {
        realmProvider = RealmProvider(loadDataInMemory = true).apply { openTransfersDb(userId = 0) }
        transferController = TransferController(realmProvider)
    }

    @AfterTest
    fun tearDown() = runTest {
        transferController.removeData()
        realmProvider.closeTransfersDb()
    }
    //endregion

    //region Get data
    @Test
    fun canGetAllTransfersFlow() = runTest {
        addTwoRandomTransfersInDatabase()
        val transfers = transferController.getAllTransfersFlow().first()
        assertEquals(2, transfers.count(), "The transfers list must contain 2 items")
    }

    @Test
    fun canGetValidTransfersFlow() = runTest {
        addTwoRandomTransfersInDatabase()

        val transferDirection = TransferDirection.SENT
        transferController.insert(DummyTransfer.expired, transferDirection, DummyTransfer.expired.password)
        transferController.insert(DummyTransfer.notExpired, transferDirection, DummyTransfer.notExpired.password)

        val validTransfers = transferController.getValidTransfersFlow(transferDirection).first()

        assertEquals(1, validTransfers.count(), "The valid transfers list must contain 1 items")
        assertEquals(
            expected = DummyTransfer.notExpired.linkUUID,
            actual = validTransfers.first().linkUUID,
            message = "The transfer in `validTransfers` should be `notExpired`",
        )
    }

    @Test
    fun canGetExpiredTransfersFlow() = runTest {
        addTwoRandomTransfersInDatabase()

        val transferDirection = TransferDirection.SENT
        transferController.insert(DummyTransfer.expired, transferDirection, DummyTransfer.expired.password)
        transferController.insert(DummyTransfer.notExpired, transferDirection, DummyTransfer.notExpired.password)

        val expiredTransfers = transferController.getExpiredTransfersFlow(transferDirection).first()

        assertEquals(1, expiredTransfers.count(), "The expired transfers list must contain 1 items")
        assertEquals(
            expected = DummyTransfer.expired.linkUUID,
            actual = expiredTransfers.first().linkUUID,
            message = "The transfer in `expiredTransfers` should be `expired`",
        )
    }

    @Test
    fun canGetTransfersCountFlow() = runTest {
        addTwoRandomTransfersInDatabase()
        val count = transferController.getTransfersCountFlow(TransferDirection.SENT).first()
        assertEquals(1, count, "The transfers list must contain 1 item")
    }

    @Test
    fun canGetTransferFlow() = runTest {
        transferController.insert(DummyTransfer.transfer1, TransferDirection.SENT, DummyTransfer.expired.password)
        val transfer = transferController.getTransferFlow(DummyTransfer.transfer1.linkUUID).first()
        assertEquals(
            expected = DummyTransfer.transfer1.linkUUID,
            actual = transfer?.linkUUID,
            message = "The transfer should be `transfer1`",
        )
    }

    @Test
    fun canGetNotReadyTransfers() = runTest {
        addTwoRandomTransfersInDatabase()
        val transfers = transferController.getNotReadyTransfers()
        assertEquals(transfers.count(), 1)
        assertEquals(transfers.first().transferStatus, TransferStatus.WAIT_VIRUS_CHECK)
    }

    @Test
    fun canFetchOrphanContainers() = runTest {
        assertEquals(0, transferController.getOrphanContainers().count(), "We should NOT have an orphan container")

        realmProvider.withTransfersDb { realm ->
            realm.write { copyToRealm(ContainerDB(DummyTransfer.container2), UpdatePolicy.ALL) }
        }

        assertEquals(1, transferController.getOrphanContainers().count(), "We should have an orphan container")
    }

    private suspend fun addTwoRandomTransfersInDatabase() {
        DummyTransfer.transfers.take(2).forEachIndexed { index, transfer ->
            val transferDirection = if (index == 0) TransferDirection.SENT else TransferDirection.RECEIVED
            transferController.insert(transfer, transferDirection, transfer.password)
        }
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

    private suspend fun canCreateTransfer(sent: TransferDirection) {
        val transfer = DummyTransfer.transfer1
        transferController.insert(transfer, sent, transfer.password)
        val realmTransfer = transferController.getTransfer(transfer.linkUUID)
        assertNotNull(realmTransfer, "The transfer cannot be null")
        assertEquals(sent, realmTransfer.transferDirection)
        assertEquals(transfer.container?.uuid, realmTransfer.container?.uuid, "The container is missing")
    }
    //endregion

    //region Update data
    @Test
    fun canUpdateAnExistingTransfer() = runTest {
        // Insert a transfer
        val transfer1 = DummyTransfer.transfer1
        val password = "password"
        transferController.insert(transfer1, TransferDirection.SENT, password)
        val realmTransfer1 = transferController.getTransfer(transfer1.linkUUID)
        assertNotNull(realmTransfer1)

        // Update the transfer
        val transfer2 = object : Transfer by transfer1 {
            override val downloadCounterCredit: Int = 12
        }
        transferController.update(transfer2)
        val realmTransfers = transferController.getAllTransfersFlow().first()
        assertNotNull(realmTransfers)
        assertEquals(1, realmTransfers.count())
        assertEquals(transfer2.downloadCounterCredit, realmTransfers.first().downloadCounterCredit)
        assertEquals(password, realmTransfer1.password)
        assertEquals(realmTransfer1.password, realmTransfers.first().password)
    }

    @Test
    fun ensureTransferStatusIsPreservedOnUpdateIfDownloadCounterCreditIsMoreThanZero() = runTest {
        val transfer = DummyTransfer.transfer3

        val initialTransferStatus = transfer.transferStatus

        assertTrue(transfer.downloadCounterCredit > 0, "downloadCounterCredit must be greater than 0")

        transferController.insert(transfer, TransferDirection.SENT, transfer.password)
        val realmTransfer = transferController.getTransfer(transfer.linkUUID)!!

        transferController.update(realmTransfer)
        val realmTransfer2 = transferController.getTransfer(transfer.linkUUID)!!

        assertEquals(initialTransferStatus, realmTransfer2.transferStatus)
    }

    @Test
    fun ensureTransferStatusIsExpiredDownloadQuotaOnUpdateIfDownloadCounterCreditIsZero() = runTest {
        val transfer = DummyTransfer.transfer4

        val initialTransferStatus = transfer.transferStatus

        assertTrue(transfer.downloadCounterCredit == 0, "downloadCounterCredit must be equal to 0")

        assertEquals(transfer.downloadCounterCredit, 0)

        transferController.insert(transfer, TransferDirection.SENT, transfer.password)
        val realmTransfer = transferController.getTransfer(transfer.linkUUID)!!

        transferController.update(realmTransfer)
        val realmTransfer2 = transferController.getTransfer(transfer.linkUUID)!!

        assertNotEquals(initialTransferStatus, realmTransfer2.transferStatus)
    }

    @Test
    fun canDeleteExpiredTransfers() = runTest {

        transferController.insert(DummyTransfer.expired, TransferDirection.SENT, password = null)
        transferController.insert(DummyTransfer.notExpired, TransferDirection.RECEIVED, password = null)

        transferController.deleteExpiredTransfers()

        val transfers = transferController.getAllTransfersFlow().first()
        assertEquals(
            expected = 1,
            actual = transfers.count(),
            message = "The transfers table should contain only 1 transfer",
        )
        assertEquals(
            expected = DummyTransfer.notExpired.linkUUID,
            actual = transfers.first().linkUUID,
            message = "The remaining transfer should be `notExpired`",
        )
    }

    @Test
    fun canRemoveTransfers() = runTest {
        transferController.insert(DummyTransfer.transfer1, TransferDirection.SENT, password = null)
        transferController.removeData()
        assertEquals(0, transferController.getAllTransfersFlow().first().count(), "The transfers table must be empty")
    }

    @Test
    fun isContainerAndFilesDeleted() = runTest {
        transferController.insert(DummyTransfer.transfer1, TransferDirection.SENT, password = null)
        transferController.deleteTransfer(DummyTransfer.transfer1.linkUUID)
        assertEquals(0, transferController.getAllTransfersFlow().first().count(), "The transfers table must be empty")
        assertEquals(0, transferController.getContainers().count(), "The containers table must be empty")
        assertEquals(0, transferController.getFiles().count(), "The files table must be empty")
    }
    //endregion
}
