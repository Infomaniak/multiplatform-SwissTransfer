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
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.controllers.AppSettingsController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.utils.EmailLanguageUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

/**
 * AccountManager is responsible for orchestrating Accounts operations using Realm for local data management.
 *
 * @property appSettingsController The controller for managing AppSettings operations.
 * @property uploadController The controller for managing Upload operations.
 * @property transferController The controller for managing Transfers operation.
 * @property realmProvider The provider for managing Realm database operations.
 */
class AccountManager internal constructor(
    private val appSettingsController: AppSettingsController,
    private val emailLanguageUtils: EmailLanguageUtils,
    private val uploadController: UploadController,
    private val transferController: TransferController,
    private val realmProvider: RealmProvider,
) {

    private val mutex = Mutex()

    // We cache the current user to avoid creating database instances when it's the same user
    private var currentUser: STUser? = null

    /**
     * Loads the specified user account and ensures database Transfers are initialized for that user.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun loadUser(user: STUser) {
        mutex.withLock {
            if (currentUser?.id != user.id) {
                appSettingsController.initAppSettings(emailLanguageUtils.getEmailLanguageFromLocal())
                realmProvider.openTransfersDb(user.id)
            }
            currentUser = user
        }
    }

    /**
     * Delete specified User data
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeUser(userId: Int) {

        appSettingsController.removeData()
        uploadController.removeData()
        transferController.removeData()

        realmProvider.closeAllDatabases()
    }
}
