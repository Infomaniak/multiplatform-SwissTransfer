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
package com.infomaniak.multiplatform_swisstransfer

import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.AppSettingsController
import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.TransfersController
import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.UploadController
import com.infomaniak.multiplatform_swisstransfer.managers.AccountManager
import com.infomaniak.multiplatform_swisstransfer.managers.AppSettingsManager
import com.infomaniak.multiplatform_swisstransfer.managers.TransferManager
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository

/**
 * SwissTransferInjection is a class responsible for initializing all the classes needed
 * to manage transfers and downloads. It replaces traditional dependency injections
 * and provides centralized access to all functionality via lazily initialized properties and methods.
 * lazily initialized properties and methods.
 *
 * This class serves as the main access point.
 *
 * @property appSettingsManager A manager used to orchestrate AppSettings operations.
 * @property transferManager A manager used to orchestrate transfer operations.
 */
class SwissTransferInjection {

    private val realmProvider by lazy { RealmProvider() }
    private val apiClientProvider by lazy { ApiClientProvider() }

    private val uploadRepository by lazy { UploadRepository(apiClientProvider) }
    private val transferRepository by lazy { TransferRepository(apiClientProvider) }

    private val appSettingsController by lazy { AppSettingsController(realmProvider) }
    private val uploadController by lazy { UploadController(realmProvider) }
    private val transfersController by lazy { TransfersController(realmProvider) }

    /** A manager used to orchestrate Transfers operations. */
    val transferManager by lazy { TransferManager(realmProvider, apiClientProvider) }

    /** A manager used to orchestrate AppSettings operations. */
    val appSettingsManager by lazy { AppSettingsManager(appSettingsController) }

    /** A manager used to orchestrate Accounts operations. */
    val accountManager by lazy { AccountManager(appSettingsController, uploadController, transfersController, realmProvider) }
}
