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
import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.dataset.DummyTransfer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class TransferControllerTest {

    private lateinit var realmProvider: RealmProvider
    private lateinit var transferController: TransferController

    @BeforeTest
    fun setup() {
        realmProvider = RealmProvider(loadDataInMemory = true).apply { openRealmTransfers(userId = 0) }
        transferController = TransferController(realmProvider)
    }

    @AfterTest
    fun tearDown() = runTest {
        transferController.removeData()
        realmProvider.closeRealmTransfers()
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
        assertEquals(2, transfers?.count(), "The transfer list must contain 2 items")
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
    fun canUpdateAnExistingTransfer() = runTest {
        // Insert a transfer
        val transfer1 = DummyTransfer.transfer1
        transferController.upsert(transfer1, TransferDirection.SENT)
        val realmTransfer1 = transferController.getTransfer(transfer1.linkUuid)
        assertNotNull(realmTransfer1)

        // Update the transfer
        val transfer2 = object : Transfer by transfer1 {
            override var containerUuid: String = "transfer2"
        }
        transferController.upsert(transfer2, TransferDirection.SENT)
        val realmTransfers = transferController.getTransfers()
        assertNotNull(realmTransfers)
        assertEquals(1, realmTransfers.count())
        assertEquals(transfer2.containerUuid, realmTransfers.first().containerUuid)
    }

    @Test
    fun canRemoveTransfers() = runTest {
        transferController.upsert(DummyTransfer.transfer1, TransferDirection.SENT)
        transferController.removeData()
        assertEquals(0, transferController.getTransfers()?.count(), "The transfers table must be empty")
    }

    private suspend fun canCreateTransfer(sent: TransferDirection) {
        val transfer = DummyTransfer.transfer1
        transferController.upsert(transfer, sent)
        val realmTransfer = transferController.getTransfer(transfer.linkUuid)
        assertNotNull(realmTransfer, "The transfer cannot be null")
        assertEquals(sent, realmTransfer.transferDirection())
        assertEquals(transfer.container?.uuid, realmTransfer.container?.uuid, "The container is missing")
    }

    private suspend fun addTwoRandomTransfersInDatabase() {
        DummyTransfer.transfers.take(2).forEachIndexed { index, transfer ->
            val transferDirection = if (index == 0) TransferDirection.SENT else TransferDirection.RECEIVED
            transferController.upsert(transfer, transferDirection)
        }
    }

    private suspend fun canGetTransfersByDirection(transferDirection: TransferDirection) {
        addTwoRandomTransfersInDatabase()
        val transfers = transferController.getTransfers(transferDirection)
        assertNotNull(transfers)
        assertEquals(1, transfers.count(), "The transfer list must contain 1 item")
        assertEquals(transferDirection, transfers.first().transferDirection())
    }
}
