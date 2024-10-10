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

import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.TransfersController
import com.infomaniak.multiplatform_swisstransfer.database.dataset.DummyTransfer
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class TransferControllerTest {

    private val realmProvider = RealmProvider().apply { openRealmTransfers(userId = 0, inMemory = true) }
    private val transferController = TransfersController(realmProvider)

    @AfterTest
    fun removeData() {
        runBlocking { transferController.removeData() }
    }

    @Test
    fun canCreateTransfer() = runBlocking {
        val transfer = DummyTransfer.transfer
        transferController.upsert(transfer)
        val realmTransfer = transferController.getTransfer(transfer.linkUuid)
        assertNotNull(realmTransfer)
        assertEquals(transfer.container.uuid, realmTransfer.container?.uuid)
        assertEquals(transfer.container.files.count(), realmTransfer.container?.files?.count())
    }

    @Test
    fun realmTransferListIsEmpty() {
        assertTrue(transferController.getTransfers()?.isEmpty() == true)
    }

    @Test
    fun canGetFrom() = runBlocking {
        val transfer = DummyTransfer.transfer
        transferController.upsert(transfer)
        val transfers = transferController.getTransfers()
        assertEquals(1, transfers?.count())
    }
}
