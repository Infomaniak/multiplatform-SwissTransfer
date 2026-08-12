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

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseConfig
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseProvider
import com.infomaniak.multiplatform_swisstransfer.database.getAppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import splitties.init.injectAsAppCtx
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration1To2Test {

    private val testDbName = "migration_1_to_2_test"
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @BeforeTest
    fun setUp() {
        context.injectAsAppCtx()
    }

    @AfterTest
    fun tearDown() {
        context.deleteDatabase(testDbName)
    }

    /**
     * Opens a pre-populated version-1 database, upgrades it to version 2, and verifies
     * that the transfer row and its related file remain intact while `senderEmail` is removed.
     */
    @Test
    fun migration1To2_preservesTransferAndFilesAndRemovesSenderEmail() = runTest {
        // Step 1: Create a version-1 database with the legacy schema (including senderEmail).
        val dbFile = context.getDatabasePath(testDbName)
        dbFile.parentFile?.mkdirs()

        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `AppSettingsDB` (
                    `id` TEXT NOT NULL, `theme` TEXT NOT NULL, `validityPeriod` TEXT NOT NULL,
                    `downloadLimit` TEXT NOT NULL, `emailLanguage` TEXT NOT NULL,
                    `lastTransferType` TEXT NOT NULL, `lastAuthorEmail` TEXT,
                    `idOfAccountWithGuestData` INTEGER, PRIMARY KEY(`id`))"""
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `DownloadManagerRef` (
                    `transferId` TEXT NOT NULL, `fileId` TEXT NOT NULL,
                    `downloadManagerUniqueId` INTEGER NOT NULL, `userOwnerId` INTEGER NOT NULL,
                    PRIMARY KEY(`transferId`, `fileId`))"""
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `TransferDB` (
                    `id` TEXT NOT NULL, `senderEmail` TEXT NOT NULL, `title` TEXT,
                    `message` TEXT, `createdAt` INTEGER NOT NULL, `expiresAt` INTEGER NOT NULL,
                    `totalSize` INTEGER NOT NULL, `password` TEXT,
                    `transferDirection` TEXT NOT NULL, `transferStatus` TEXT NOT NULL,
                    `recipientsEmails` TEXT NOT NULL, `linkId` TEXT, `userOwnerId` INTEGER NOT NULL,
                    PRIMARY KEY(`id`))"""
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `FileDB` (
                    `id` TEXT NOT NULL, `path` TEXT NOT NULL, `size` INTEGER NOT NULL,
                    `mimeType` TEXT, `isFolder` INTEGER NOT NULL, `thumbnailPath` TEXT,
                    `transferId` TEXT NOT NULL, `parentId` TEXT, PRIMARY KEY(`id`),
                    FOREIGN KEY(`transferId`) REFERENCES `TransferDB`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE)"""
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_FileDB_transferId` ON `FileDB` (`transferId`)"
            )

            // Room uses room_master_table to verify the schema identity hash before migration.
            db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            db.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, 'd70e7f13f5355ab969659ba9acdbfb0e')")

            // Populate with a transfer row that includes senderEmail and an associated file.
            db.execSQL(
                """INSERT INTO TransferDB
                    (id, senderEmail, title, message, createdAt, expiresAt, totalSize, password,
                     transferDirection, transferStatus, recipientsEmails, linkId, userOwnerId)
                    VALUES ('transfer-v1-id', 'sender@example.com', 'My Transfer', NULL,
                            1000, 4102441200, 10000, NULL, 'SENT', 'READY', '[]', NULL, 0)"""
            )
            db.execSQL(
                """INSERT INTO FileDB
                    (id, path, size, mimeType, isFolder, thumbnailPath, transferId, parentId)
                    VALUES ('file-v1-id', '/documents/report.pdf', 10000, 'application/pdf',
                            0, NULL, 'transfer-v1-id', NULL)"""
            )

            // Mark the database as version 1 so that Room triggers the auto-migration.
            db.version = 1
        }

        // Step 2: Open the database through Room, which will auto-migrate from version 1 to 2.
        val databaseProvider = DatabaseProvider(DatabaseConfig(databaseNameOrPath = testDbName))
        val appDatabase = databaseProvider.getAppDatabase(inMemory = false, driver = AndroidSQLiteDriver())

        try {
            val transferDao = appDatabase.getTransferDao()

            // Verify the transfer row survived the migration.
            val transfer = transferDao.getTransfer("transfer-v1-id")
            assertNotNull(transfer, "Transfer must survive the 1→2 migration")
            assertEquals("transfer-v1-id", transfer.id)
            assertEquals("My Transfer", transfer.title)
            assertEquals(TransferStatus.READY, transfer.transferStatus)

            // Verify the related file row survived the migration.
            val files = transferDao.getTransferFiles("transfer-v1-id")
            assertEquals(1, files.size, "File must survive the 1→2 migration")
            assertEquals("file-v1-id", files[0].id)
            assertEquals("/documents/report.pdf", files[0].path)
        } finally {
            appDatabase.close()
        }

        // Verify that senderEmail no longer exists as a column in TransferDB.
        // We open a fresh raw-driver connection after Room has closed to avoid locking conflicts.
        val columns = AndroidSQLiteDriver().open(dbFile.absolutePath).use { connection ->
            connection.prepare("PRAGMA table_info(TransferDB)").use { stmt ->
                buildList {
                    while (stmt.step()) {
                        // PRAGMA table_info columns: cid, name, type, notnull, dflt_value, pk
                        add(stmt.getText(1))
                    }
                }
            }
        }
        assertFalse(columns.contains("senderEmail"), "senderEmail must be removed after migration")
    }
}
