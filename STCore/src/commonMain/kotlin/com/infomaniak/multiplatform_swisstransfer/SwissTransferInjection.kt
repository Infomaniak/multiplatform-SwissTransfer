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

import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.controllers.*
import com.infomaniak.multiplatform_swisstransfer.managers.*
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
 * @property environment Customize client api base url, for example [ApiEnvironment.Prod]
 * @property userAgent Customize client api userAgent.
 * @property databaseRootDirectory Customize root directory for realm, eg. iOS app group container.
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
    private val databaseRootDirectory: String? = null,
) {

    private val realmProvider by lazy { RealmProvider(databaseRootDirectory) }
    private val apiClientProvider by lazy { ApiClientProvider(userAgent) }

    private val uploadRepository by lazy { UploadRepository(apiClientProvider, environment) }
    private val transferRepository by lazy { TransferRepository(apiClientProvider, environment) }

    private val appSettingsController by lazy { AppSettingsController(realmProvider) }
    private val uploadTokensController by lazy { UploadTokensController(realmProvider) }
    private val uploadController by lazy { UploadController(realmProvider) }
    private val transferController by lazy { TransferController(realmProvider) }
    private val fileController by lazy { FileController(realmProvider) }

    private val emailLanguageUtils by lazy { EmailLanguageUtils() }

    /** A manager used to orchestrate Transfers operations. */
    val transferManager by lazy { TransferManager(apiClientProvider, transferController, transferRepository) }

    /** A manager used to orchestrate Files operations. */
    val fileManager by lazy { FileManager(fileController) }

    /** A manager used to orchestrate AppSettings operations. */
    val appSettingsManager by lazy { AppSettingsManager(appSettingsController) }

    /** A manager used to orchestrate uploads' tokens operations (email token or attestation token). */
    val uploadTokensManager by lazy { UploadTokensManager(uploadTokensController) }

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
    val uploadManager by lazy {
        UploadManager(
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
            fileController,
            transferController,
            uploadController,
            transferRepository,
        )
    }
}
