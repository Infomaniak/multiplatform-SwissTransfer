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

import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class RealmProviderTest {

    private lateinit var realmProvider: RealmProvider

    @BeforeTest
    fun setup() = runTest {
        realmProvider = RealmProvider(loadDataInMemory = true).apply { openTransfersDb(userId = 0) }
    }

    @AfterTest
    fun tearDown() = runTest {
        if (realmProvider.transfersAsync.isCompleted) {
            realmProvider.closeTransfersDb()
        }
    }

    @Test
    fun canSwitchUserByClosingPreviousUser() = runTest {
        // Login user
        realmProvider.openTransfersDb(1)
        val previousRealm = realmProvider.transfersAsync.await()
        val oldTransferJob = realmProvider.transfersAsync.job
        assertEquals(true, realmProvider.transfersAsync.isCompleted)

        // Switch user account with another user
        realmProvider.openTransfersDb(2)
        assertNotEquals(oldTransferJob, realmProvider.transfersAsync.job, "The new switched account must have its own job")
        assertTrue(previousRealm.isClosed(), "The realm of the previous user must be closed")
    }

    @Test
    fun canCloseTransfersDb() = runTest {
        realmProvider.openTransfersDb(1)
        val transfersRealm = realmProvider.transfersAsync.await()
        assertFalse(transfersRealm.isClosed())

        realmProvider.closeTransfersDb()
        assertTrue(transfersRealm.isClosed())
    }
}
