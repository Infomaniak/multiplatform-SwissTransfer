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

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.CrashReportInterface
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseConfig
import com.infomaniak.multiplatform_swisstransfer.database.DatabaseProvider
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.controllers.AppSettingsController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.FileController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadTokensController
import com.infomaniak.multiplatform_swisstransfer.database.getAppDatabase
import com.infomaniak.multiplatform_swisstransfer.managers.AccountManager
import com.infomaniak.multiplatform_swisstransfer.managers.AppSettingsManager
import com.infomaniak.multiplatform_swisstransfer.managers.FileManager
import com.infomaniak.multiplatform_swisstransfer.managers.InMemoryUploadManager
import com.infomaniak.multiplatform_swisstransfer.managers.TransferManager
import com.infomaniak.multiplatform_swisstransfer.managers.UploadManager
import com.infomaniak.multiplatform_swisstransfer.managers.UploadTokensManager
import com.infomaniak.multiplatform_swisstransfer.managers.UploadV2Manager
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferRepository
import com.infomaniak.multiplatform_swisstransfer.network.repositories.TransferV2Repository
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadRepository
import com.infomaniak.multiplatform_swisstransfer.network.repositories.UploadV2Repository
import com.infomaniak.multiplatform_swisstransfer.utils.EmailLanguageUtils

/**
 * SwissTransferInjection is a class responsible for initializing all the classes needed
 * to manage transfers and downloads. It replaces traditional dependency injections
 * and provides centralized access to all functionality via lazily initialized properties and methods.
 * lazily initialized properties and methods.
 *
 * This class serves as the main access point.
 *
 * @property environment Customize client api base url, for example [ApiEnvironment.Prod]
 * @property userAgent Customize client api userAgent.
 * @property legacyDatabaseRootDirectory Customize root directory for realm, eg. iOS app group container.
 * @property databaseNameOrPath Full file path on iOS, just name (without extension) on Android.
 * @property crashReport A crash report interface used to report errors and add breadcrumbs.
 * @property transferManager A manager used to orchestrate transfer operations.
 * @property appSettingsManager A manager used to orchestrate AppSettings operations.
 * @property accountManager A manager used to orchestrate Accounts operations.
 * @property inMemoryUploadManager A manager used to perform Uploads operations, without session persistence.
 * @property uploadManager A manager used to orchestrate Uploads operations.
 * @property uploadTokensManager A manager used to orchestrate operations of tokens needed for the upload.
 * @property sharedApiUrlCreator An utils to help use shared routes.
 */
class SwissTransferInjection(
    private val environment: ApiEnvironment,
    private val userAgent: String,
    private val legacyDatabaseRootDirectory: String? = null,
    private val databaseNameOrPath: String,
    private val crashReport: CrashReportInterface,
) {

    private val requireToken: () -> String = {
        (accountManager.currentUser as? STUser.AuthUser)?.token
            ?: throw UnknownException(Exception("No user logged in"))
    }

    private val realmProvider by lazy { RealmProvider(legacyDatabaseRootDirectory) }
    private val appDatabase by lazy {
        val databaseConfig = DatabaseConfig(databaseNameOrPath = databaseNameOrPath)
        DatabaseProvider(databaseConfig).getAppDatabase()
    }
    private val apiClientProvider by lazy { ApiClientProvider(userAgent, crashReport) }

    private val uploadRepository by lazy { UploadRepository(apiClientProvider, environment) }
    private val uploadV2Repository by lazy { UploadV2Repository(apiClientProvider, environment, requireToken) }
    private val transferRepository by lazy { TransferRepository(apiClientProvider, environment) }
    private val transferV2Repository by lazy { TransferV2Repository(apiClientProvider, environment, requireToken) }

    private val appSettingsController by lazy { AppSettingsController(realmProvider) }
    private val uploadTokensController by lazy { UploadTokensController(realmProvider) }
    private val uploadController by lazy { UploadController(realmProvider) }
    private val transferController by lazy { TransferController(realmProvider) }
    private val fileController by lazy { FileController(realmProvider) }

    private val emailLanguageUtils by lazy { EmailLanguageUtils() }

    /** A manager used to orchestrate Transfers operations. */
    val transferManager by lazy {
        TransferManager(
            accountManager,
            appDatabase,
            transferController,
            transferRepository,
            transferV2Repository,
        )
    }

    /** A manager used to orchestrate Files operations. */
    val fileManager by lazy { FileManager(accountManager, appDatabase, fileController) }

    /** A manager used to orchestrate AppSettings operations. */
    val appSettingsManager by lazy { AppSettingsManager(appDatabase, appSettingsController, realmProvider, emailLanguageUtils) }

    /** A manager used to orchestrate uploads' tokens operations (email token or attestation token). */
    val uploadTokensManager by lazy { UploadTokensManager(uploadTokensController) }

    /** A manager used to orchestrate Accounts operations. */
    val accountManager by lazy {
        AccountManager(
            appDatabase = appDatabase,
            uploadController = uploadController,
            transferController = transferController,
            appSettingsManager = appSettingsManager,
            realmProvider = realmProvider,
        )
    }

    val uploadV2Manager by lazy {
        UploadV2Manager(
            accountManager = accountManager,
            uploadRepository = uploadV2Repository,
            transferManager = transferManager,
            appDatabase = appDatabase,
            uploadDao = appDatabase.getUploadDao(),
            transferDao = appDatabase.getTransferDao(),
        )
    }

    /** A manager used to orchestrate Uploads operations. */
    val uploadManager by lazy {
        UploadManager(
            appDatabase,
            accountManager,
            uploadController,
            uploadRepository,
            transferManager,
            emailLanguageUtils,
            uploadTokensManager,
        )
    }

    /** A manager used to perform Uploads operations, without session persistence. */
    val inMemoryUploadManager by lazy {
        InMemoryUploadManager(
            uploadController,
            uploadRepository,
            transferManager,
            emailLanguageUtils,
            uploadTokensManager,
        )
    }

    /** An utils to help use shared routes  */
    val sharedApiUrlCreator by lazy {
        SharedApiUrlCreator(
            environment,
            appDatabase,
            fileController,
            transferController,
            uploadController,
            transferRepository,
        )
    }
}
