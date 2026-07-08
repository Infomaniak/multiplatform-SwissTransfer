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
package com.infomaniak.multiplatform_swisstransfer.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class MigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = AppDatabase::class.java,
    )

    @Test
    fun migrate1To2() {
        // Create the database with version 1
        helper.createDatabase(testDbName, 1).use {
            // No data insertion needed for this specific migration test as it's an auto-migration
            // that adds an index. Validation will be handled by runMigrationsAndValidate.
        }

        // Run migration to version 2
        helper.runMigrationsAndValidate(testDbName, 2, true).use { connection ->
            // Verify that the new index exists
            connection.query("SELECT name FROM sqlite_master WHERE type='index' AND name='index_TransferDB_createdAt'").use { cursor ->
                assertTrue(cursor.moveToFirst(), "Index 'index_TransferDB_createdAt' should exist after migration to v2")
            }
        }
    }
}
