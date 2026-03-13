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

import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseProvider
import com.infomaniak.multiplatform_swisstransfer.database.getAppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.DownloadManagerRef
import com.infomaniak.multiplatform_swisstransfer.database.v2.utils.DatabaseConfigFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DownloadManagerRefDaoTest : RobolectricTestsBase() {
    private lateinit var appDatabase: AppDatabase

    @BeforeTest
    fun createDb() {
        val databaseProvider = DatabaseProvider(DatabaseConfigFactory.createTestDatabaseConfig())
        appDatabase = databaseProvider.getAppDatabase(inMemory = true, driver = DatabaseConfigFactory.driver())
    }

    @AfterTest
    fun closeDb() {
        appDatabase.close()
    }

    //region getDownloadManagerId tests
    @Test
    fun getDownloadManagerId_withExistingRef_returnsId() = runTest {
        val ref = createDownloadManagerRef("transfer1", "file1", 123L)

        appDatabase.getDownloadManagerRef().update(ref)

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1").first()
        assertNotNull(result)
        assertEquals(123L, result)
    }

    @Test
    fun getDownloadManagerId_withNonExistingRef_returnsNull() = runTest {
        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("nonexistent", "nonexistent").first()

        assertNull(result)
    }

    @Test
    fun getDownloadManagerId_withDefaultFileId_returnsIdForEmptyFileId() = runTest {
        val ref = createDownloadManagerRef("transfer1", "", 456L)
        appDatabase.getDownloadManagerRef().update(ref)

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1").first()

        assertNotNull(result)
        assertEquals(456L, result)
    }

    @Test
    fun getDownloadManagerId_withDifferentTransferId_returnsNull() = runTest {
        val ref = createDownloadManagerRef("transfer1", "file1", 123L)
        appDatabase.getDownloadManagerRef().update(ref)

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer2", "file1").first()

        assertNull(result)
    }

    @Test
    fun getDownloadManagerId_returnsFlow_emitsUpdates() = runTest {
        val ref1 = createDownloadManagerRef("transfer1", "file1", 100L)
        appDatabase.getDownloadManagerRef().update(ref1)

        val flow = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1")

        // First emission
        assertEquals(100L, flow.first())

        // Update and check new emission
        val ref2 = createDownloadManagerRef("transfer1", "file1", 200L)
        appDatabase.getDownloadManagerRef().update(ref2)

        assertEquals(200L, flow.first())
    }
    //endregion

    //region update tests
    @Test
    fun update_insertNewRef_insertsSuccessfully() = runTest {
        val ref = createDownloadManagerRef("transfer1", "file1", 123L)

        appDatabase.getDownloadManagerRef().update(ref)

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1").first()
        assertNotNull(result)
        assertEquals(123L, result)
    }

    @Test
    fun update_existingRef_updatesValue() = runTest {
        val ref1 = createDownloadManagerRef("transfer1", "file1", 100L)
        appDatabase.getDownloadManagerRef().update(ref1)

        val ref2 = createDownloadManagerRef("transfer1", "file1", 200L)
        appDatabase.getDownloadManagerRef().update(ref2)

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1").first()
        assertEquals(200L, result)
    }

    @Test
    fun update_multipleDifferentRefs_allStored() = runTest {
        val ref1 = createDownloadManagerRef("transfer1", "file1", 100L)
        val ref2 = createDownloadManagerRef("transfer1", "file2", 200L)
        val ref3 = createDownloadManagerRef("transfer2", "file1", 300L)

        appDatabase.getDownloadManagerRef().update(ref1)
        appDatabase.getDownloadManagerRef().update(ref2)
        appDatabase.getDownloadManagerRef().update(ref3)

        assertEquals(100L, appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1").first())
        assertEquals(200L, appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file2").first())
        assertEquals(300L, appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer2", "file1").first())
    }

    @Test
    fun update_withEmptyFileId_storesCorrectly() = runTest {
        val ref = createDownloadManagerRef("transfer1", "", 456L)

        appDatabase.getDownloadManagerRef().update(ref)

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "").first()
        assertEquals(456L, result)
    }
    //endregion

    //region delete tests
    @Test
    fun delete_existingRef_removesIt() = runTest {
        val ref = createDownloadManagerRef("transfer1", "file1", 123L)
        appDatabase.getDownloadManagerRef().update(ref)

        appDatabase.getDownloadManagerRef().delete("transfer1", "file1")

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1").first()
        assertNull(result)
    }

    @Test
    fun delete_nonExistingRef_doesNothing() = runTest {
        // Should not throw
        appDatabase.getDownloadManagerRef().delete("nonexistent", "nonexistent")
    }

    @Test
    fun delete_withDefaultFileId_deletesRefWithEmptyFileId() = runTest {
        val ref = createDownloadManagerRef("transfer1", "", 456L)
        appDatabase.getDownloadManagerRef().update(ref)

        appDatabase.getDownloadManagerRef().delete("transfer1")

        val result = appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "").first()
        assertNull(result)
    }

    @Test
    fun delete_onlyDeletesMatchingRef() = runTest {
        val ref1 = createDownloadManagerRef("transfer1", "file1", 100L)
        val ref2 = createDownloadManagerRef("transfer1", "file2", 200L)
        val ref3 = createDownloadManagerRef("transfer2", "file1", 300L)

        appDatabase.getDownloadManagerRef().update(ref1)
        appDatabase.getDownloadManagerRef().update(ref2)
        appDatabase.getDownloadManagerRef().update(ref3)

        appDatabase.getDownloadManagerRef().delete("transfer1", "file1")

        assertNull(appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1").first())
        assertEquals(200L, appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file2").first())
        assertEquals(300L, appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer2", "file1").first())
    }
    //endregion

    //region deleteAll tests
    @Test
    fun deleteAll_removesAllRefsForUser() = runTest {
        val ref1 = createDownloadManagerRef("transfer1", "file1", 100L, userId = 0L)
        val ref2 = createDownloadManagerRef("transfer1", "file2", 200L, userId = 0L)
        val ref3 = createDownloadManagerRef("transfer2", "file1", 300L, userId = 0L)

        appDatabase.getDownloadManagerRef().update(ref1)
        appDatabase.getDownloadManagerRef().update(ref2)
        appDatabase.getDownloadManagerRef().update(ref3)

        appDatabase.getDownloadManagerRef().deleteAll(0L)

        assertNull(appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file1").first())
        assertNull(appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer1", "file2").first())
        assertNull(appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer2", "file1").first())
    }

    @Test
    fun deleteAll_onlyDeletesRefsForSpecifiedUser() = runTest {
        val refUser0 = createDownloadManagerRef("transfer_user0", "file1", 100L, userId = 0L)
        val refUser1 = createDownloadManagerRef("transfer_user1", "file1", 200L, userId = 1L)

        appDatabase.getDownloadManagerRef().update(refUser0)
        appDatabase.getDownloadManagerRef().update(refUser1)

        appDatabase.getDownloadManagerRef().deleteAll(0L)

        assertEquals(200L, appDatabase.getDownloadManagerRef().getDownloadManagerId("transfer_user1", "file1").first())
    }

    @Test
    fun deleteAll_withNoRefsForUser_doesNothing() = runTest {
        // Should not throw
        appDatabase.getDownloadManagerRef().deleteAll(999L)
    }
    //endregion

    //region Helper methods
    private fun createDownloadManagerRef(
        transferId: String,
        fileId: String,
        downloadManagerUniqueId: Long,
        userId: Long = 0L,
    ): DownloadManagerRef = DownloadManagerRef(
        transferId = transferId,
        fileId = fileId,
        downloadManagerUniqueId = downloadManagerUniqueId,
        userOwnerId = userId,
    )
    //endregion
}
