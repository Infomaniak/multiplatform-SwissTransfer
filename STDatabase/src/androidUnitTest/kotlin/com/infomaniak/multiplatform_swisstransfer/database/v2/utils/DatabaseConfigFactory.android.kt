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
package com.infomaniak.multiplatform_swisstransfer.database.v2.utils

import android.app.Application
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseConfig

actual object DatabaseConfigFactory {

    actual fun createTestDatabaseConfig(): DatabaseConfig {
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        return DatabaseConfig(
            appContext = appContext,
            databaseRootDirectory = appContext.cacheDir.absolutePath
        )
    }

    actual fun driver(): SQLiteDriver = AndroidSQLiteDriver()
}
