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
import com.infomaniak.multiplatform_swisstransfer.database.controllers.AppSettingsController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.managers.AccountManager
import com.infomaniak.multiplatform_swisstransfer.managers.AppSettingsManager
import com.infomaniak.multiplatform_swisstransfer.managers.TransferManager
import com.infomaniak.multiplatform_swisstransfer.managers.UploadManager
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository
import com.infomaniak.multiplatform_swisstransfer.utils.EmailLanguageUtils

/**
 * SwissTransferInjection is a class responsible for initializing all the classes needed
 * to manage transfers and downloads. It replaces traditional dependency injections
 * and provides centralized access to all functionality via lazily initialized properties and methods.
 * lazily initialized properties and methods.
 *
 * This class serves as the main access point.
 *
 * @property userAgent Customize client api userAgent.
 * @property transferManager A manager used to orchestrate transfer operations.
 * @property appSettingsManager A manager used to orchestrate AppSettings operations.
 * @property accountManager A manager used to orchestrate Accounts operations.
 * @property uploadManager A manager used to orchestrate Uploads operations.
 * @property sharedApiUrlCreator An utils to help use shared routes.
 */
class SwissTransferInjection(
    private val userAgent: String,
) {

    private val realmProvider by lazy { RealmProvider() }
    private val apiClientProvider by lazy { ApiClientProvider(userAgent) }

    private val uploadRepository by lazy { UploadRepository(apiClientProvider) }
    private val transferRepository by lazy { TransferRepository(apiClientProvider) }

    private val appSettingsController by lazy { AppSettingsController(realmProvider) }
    private val uploadController by lazy { UploadController(realmProvider) }
    private val transferController by lazy { TransferController(realmProvider) }

    private val emailLanguageUtils by lazy { EmailLanguageUtils() }

    /** A manager used to orchestrate Transfers operations. */
    val transferManager by lazy { TransferManager(apiClientProvider, transferController, transferRepository) }

    /** A manager used to orchestrate AppSettings operations. */
    val appSettingsManager by lazy { AppSettingsManager(appSettingsController) }

    /** A manager used to orchestrate Accounts operations. */
    val accountManager by lazy {
        AccountManager(
            appSettingsController = appSettingsController,
            emailLanguageUtils = emailLanguageUtils,
            uploadController = uploadController,
            transferController = transferController,
            realmProvider = realmProvider,
        )
    }

    /** A manager used to orchestrate Uploads operations. */
    val uploadManager by lazy { UploadManager(uploadController, uploadRepository, transferManager) }

    /** An utils to help use shared routes  */
    val sharedApiUrlCreator by lazy { SharedApiUrlCreator(transferController, uploadController) }
}
