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
package com.infomaniak.multiplatform_swisstransfer.managers

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.controllers.AppSettingsController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.v2.AppSettingsDB
import com.infomaniak.multiplatform_swisstransfer.utils.EmailLanguageUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

/**
 * AccountManager is responsible for orchestrating Accounts operations, with the database(s).
 *
 * @property appDatabase The provider for managing Room database operations.
 * @property appSettingsController The controller for managing AppSettings operations.
 * @property uploadController The controller for managing Upload operations.
 * @property transferController The controller for managing Transfers operation.
 * @property realmProvider The provider for managing Realm database operations.
 */
class AccountManager internal constructor(
    private val appDatabase: AppDatabase,
    private val appSettingsController: AppSettingsController,
    private val emailLanguageUtils: EmailLanguageUtils,
    private val uploadController: UploadController,
    private val transferController: TransferController,
    private val realmProvider: RealmProvider,
) {

    private val mutex = Mutex()

    // We cache the current user to avoid creating database instances when it's the same user
    var currentUser: STUser? = null
        private set

    /**
     * Loads the specified user account and ensures database Transfers are initialized for that user.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun loadUser(user: STUser) {
        mutex.withLock {
            if (currentUser?.id != user.id) loadDatabase(user)
            currentUser = user
        }
    }

    /**
     * Delete specified User data
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun logoutCurrentUser(newSTUser: STUser?) {
        val user = currentUser ?: return
        if (user is STUser.AuthUser) {
            appDatabase.getTransferDao().deleteTransfers(user.id)
        } else {
            uploadController.removeData()
            transferController.removeData()
            realmProvider.closeAllDatabases()
        }
        mutex.withLock {
            currentUser = newSTUser
        }
    }

    private suspend fun loadDatabase(user: STUser) {
        when (user) {
            is STUser.GuestUser -> {
                appSettingsController.initAppSettings(emailLanguageUtils.getEmailLanguageFromLocal())
                realmProvider.openTransfersDb(user.id)
            }
            is STUser.AuthUser if currentUser is STUser.GuestUser && appDatabase.isMigrationNeeded() -> {
                makeMigrationFromOldDB(appSettingsController, appDatabase)
            }
            else -> Unit
        }
    }
}

private suspend fun AppDatabase.isMigrationNeeded(): Boolean {
    val appSettings = getAppSettingsDao().getAppSettings().first()
    return appSettings?.dataMigrated != true
}

private suspend fun makeMigrationFromOldDB(appSettingsController: AppSettingsController, appDatabase: AppDatabase) {
    val oldAppSettings = appSettingsController.getAppSettings() ?: return
    val appSettingsDB = AppSettingsDB(oldAppSettings, dataMigrated = true)
    appDatabase.getAppSettingsDao().put(appSettingsDB)
}
