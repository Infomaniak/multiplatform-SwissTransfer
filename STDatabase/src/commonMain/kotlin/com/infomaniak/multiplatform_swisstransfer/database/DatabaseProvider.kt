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

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.infomaniak.multiplatform_swisstransfer.database.dao.AppSettingsDao
import com.infomaniak.multiplatform_swisstransfer.database.dao.DownloadManagerRefDao
import com.infomaniak.multiplatform_swisstransfer.database.dao.TransferDao
import com.infomaniak.multiplatform_swisstransfer.database.dao.UploadDao
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.v2.AppSettingsDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.DownloadManagerRef
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

expect class DatabaseProvider {
    constructor(databaseConfig: DatabaseConfig)

    internal fun getRoomDatabaseBuilder(inMemory: Boolean): RoomDatabase.Builder<AppDatabase>
}

fun DatabaseProvider.getAppDatabase(
    inMemory: Boolean = false,
    driver: SQLiteDriver = BundledSQLiteDriver(),
): AppDatabase {
    return getRoomDatabaseBuilder(inMemory)
        .setDriver(driver)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@Database(
    entities = [AppSettingsDB::class, DownloadManagerRef::class, TransferDB::class, FileDB::class],
    version = 1
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAppSettingsDao(): AppSettingsDao
    abstract fun getDownloadManagerRef(): DownloadManagerRefDao
    abstract fun getTransferDao(): TransferDao
    abstract fun getUploadDao(): UploadDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
