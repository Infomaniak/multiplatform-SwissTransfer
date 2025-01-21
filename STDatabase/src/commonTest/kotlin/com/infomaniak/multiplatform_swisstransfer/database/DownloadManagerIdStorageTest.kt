/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.dataset.DummyTransfer
import com.infomaniak.multiplatform_swisstransfer.database.extensions.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.test.AfterTest
import kotlin.test.Test

class DownloadManagerIdStorageTest {

    private val realmProvider: RealmProvider by lazy(NONE) {
        RealmProvider(loadDataInMemory = true).apply { openTransfersDb(userId = 0) }
    }
    private val transferController: TransferController by lazy(NONE) {
        TransferController(realmProvider)
    }

    @AfterTest
    fun tearDown() = runTest {
        transferController.removeData()
        realmProvider.closeTransfersDb()
    }

    private val downloadManagerIds = object {
        private var lastId: Long = 0L
        val refFirstFile = ++lastId
        val refSecondFile = ++lastId
        val refZip = ++lastId
        val firstFile = ++lastId
        val secondFile = ++lastId
        val zip = ++lastId
    }

    private suspend fun prepareReferenceTransfer(referenceTransfer: Transfer) {
        val file1 = referenceTransfer.container!!.files[0]
        val file2 = referenceTransfer.container!!.files[1]
        writeDlManagerId(
            transfer = referenceTransfer,
            file = file1,
            uniqueId = downloadManagerIds.refFirstFile
        )
        writeDlManagerId(
            transfer = referenceTransfer,
            file = file2,
            uniqueId = downloadManagerIds.refSecondFile
        )
        writeDlManagerId(
            transfer = referenceTransfer,
            file = null,
            uniqueId = downloadManagerIds.refZip
        )
    }

    private suspend fun checkReferenceWasUnchanged(referenceTransfer: Transfer) {
        val file1 = referenceTransfer.container!!.files[0]
        val file2 = referenceTransfer.container!!.files[1]
        readDlManagerId(referenceTransfer, file1) shouldBe downloadManagerIds.refFirstFile
        readDlManagerId(referenceTransfer, file2) shouldBe downloadManagerIds.refSecondFile
        readDlManagerId(referenceTransfer, file = null) shouldBe downloadManagerIds.refZip
    }

    @Test
    fun checkItAll() = runTest {
        val targetTransfer = DummyTransfer.expired
        /** Will be used to prove operations on `targetTransfer` don't affect other transfers. */
        val referenceTransfer = DummyTransfer.notExpired
        transferController.upsert(referenceTransfer, TransferDirection.RECEIVED, password = null)
        transferController.upsert(targetTransfer, TransferDirection.RECEIVED, password = null)

        prepareReferenceTransfer(referenceTransfer)

        run {
            val file1 = targetTransfer.container!!.files[0]
            val file2 = targetTransfer.container!!.files[1]
            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = downloadManagerIds.firstFile)
            writeDlManagerId(transfer = targetTransfer, file = file2, uniqueId = downloadManagerIds.secondFile)
            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = downloadManagerIds.zip)

            // Check what we inserted is stored properly.
            readDlManagerId(targetTransfer, file1) shouldBe downloadManagerIds.firstFile
            readDlManagerId(targetTransfer, file2) shouldBe downloadManagerIds.secondFile
            readDlManagerId(targetTransfer, file = null) shouldBe downloadManagerIds.zip

            // Check removing file1's DL manager id removes only file1's DL manager id.
            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = null)
            readDlManagerId(targetTransfer, file1) shouldBe null
            readDlManagerId(targetTransfer, file2) shouldBe downloadManagerIds.secondFile
            readDlManagerId(targetTransfer, file = null) shouldBe downloadManagerIds.zip

            // Check removing the zip DL manager id removes only the zip DL manager id.
            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = null)
            readDlManagerId(targetTransfer, file1) shouldBe null
            readDlManagerId(targetTransfer, file2) shouldBe downloadManagerIds.secondFile
            readDlManagerId(targetTransfer, file = null) shouldBe null

            // Put back DL manager ids prior to subsequent checks.
            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = downloadManagerIds.firstFile)
            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = downloadManagerIds.zip)
            // Check deleting the transfer deletes all related DL manager ids.
            transferController.deleteTransfer(targetTransfer.linkUUID)
            readDlManagerId(targetTransfer, file1) shouldBe null
            readDlManagerId(targetTransfer, file2) shouldBe null
            readDlManagerId(targetTransfer, file = null) shouldBe null

            // Check deleting expired transfers deletes their related DL manager ids.
            transferController.upsert(targetTransfer, TransferDirection.RECEIVED, password = null)
            writeDlManagerId(transfer = targetTransfer, file = file1, uniqueId = downloadManagerIds.firstFile)
            writeDlManagerId(transfer = targetTransfer, file = file2, uniqueId = downloadManagerIds.secondFile)
            writeDlManagerId(transfer = targetTransfer, file = null, uniqueId = downloadManagerIds.zip)
            transferController.deleteExpiredTransfers()
        }

        checkReferenceWasUnchanged(referenceTransfer)
    }

    private suspend fun readDlManagerId(transfer: Transfer, file: File?): Long? {
        return transferController.downloadManagerIdFor(transferUUID = transfer.linkUUID, fileUid = file?.uuid).first()
    }

    private suspend fun writeDlManagerId(transfer: Transfer, file: File?, uniqueId: Long?) {
        transferController.writeDownloadManagerId(
            transferUUID = transfer.linkUUID,
            fileUid = file?.uuid,
            uniqueDownloadManagerId = uniqueId
        )
    }
}
