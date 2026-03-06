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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.infomaniak.multiplatform_swisstransfer.managers

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

/**
 * AccountManager is responsible for orchestrating Accounts operations, with the database(s).
 *
 * @property appDatabase The provider for managing Room database operations.
 * @property uploadController The controller for managing Upload operations.
 * @property transferController The controller for managing Transfers operation.
 * @property realmProvider The provider for managing Realm database operations.
 */
class AccountManager internal constructor(
    private val appDatabase: AppDatabase,
    private val uploadController: UploadController,
    private val transferController: TransferController,
    private val appSettingsManager: AppSettingsManager,
    private val realmProvider: RealmProvider,
) {

    private val userSwitchMutex = Mutex()

    private val _currentUserFlow = MutableStateFlow<STUser?>(value = null)
    val currentUserFlow: StateFlow<STUser?> = _currentUserFlow.asStateFlow()

    // We cache the current user to avoid creating database instances when it's the same user
    var currentUser: STUser? by _currentUserFlow::value
        private set

    val showGuestData: Flow<Boolean> = currentUserFlow.transformLatest { currentUser ->
        when (currentUser) {
            STUser.GuestUser -> emit(true)
            is STUser.AuthUser -> emitAll(appSettingsManager.appSettings.map { appSettings ->
                appSettings?.idOfAccountWithGuestData == currentUser.id
            })
            null -> emit(false)
        }
    }.distinctUntilChanged()

    fun shouldShowGuestData(targetUser: STUser.AuthUser): Flow<Boolean> {
        return appSettingsManager.appSettings.map { appSettings ->
            appSettings?.idOfAccountWithGuestData == targetUser.id
        }
    }

    val shouldUseV1Api: Boolean get() = currentUser is STUser.GuestUser

    /**
     * Loads the specified user account and ensures database Transfers are initialized for that user.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun loadUser(user: STUser) {
        userSwitchMutex.withLock {
            if (currentUser is STUser.GuestUser && user is STUser.AuthUser) {
                appSettingsManager.updateLinkGuestToAccountIfNeeded(accountId = user.id)
            }
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
        when (user) {
            is STUser.AuthUser -> appDatabase.getTransferDao().deleteTransfers(user.id)
            STUser.GuestUser -> {
                uploadController.removeData()
                transferController.removeData()
                realmProvider.closeAllDatabases()
            }
        }
        userSwitchMutex.withLock {
            if (newSTUser == null) {
                currentUser = null
            } else {
                loadUser(newSTUser)
            }
            if (newSTUser !is STUser.AuthUser) {
                appSettingsManager.updateLinkGuestToAccountIfNeeded(accountId = null)
            }
        }
    }

    private suspend fun loadDatabase(user: STUser) {
        realmProvider.openTransfersDb(user.id)
    }
}
