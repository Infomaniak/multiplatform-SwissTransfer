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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferType
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.controllers.AppSettingsController
import com.infomaniak.multiplatform_swisstransfer.database.dao.AppSettingsDao
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.v2.AppSettingsDB
import com.infomaniak.multiplatform_swisstransfer.utils.EmailLanguageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Provides a convenient interface for managing application settings, abstracting the underlying
 * [AppSettingsController]. It handles asynchronous operations and ensures settings updates are
 * performed on a background thread.
 **/
class AppSettingsManager internal constructor(
    private val appDatabase: AppDatabase,
    private val appSettingsController: AppSettingsController,
    private val realmProvider: RealmProvider,
    private val emailLanguageUtils: EmailLanguageUtils,
) {

    private val dao get() = appDatabase.getAppSettingsDao()

    /**
     * A [Flow] that emits the current [AppSettings] object whenever it changes.
     */
    /**
     * A [Flow] that emits the current [AppSettings] object whenever it changes.
     */
    val appSettings: Flow<AppSettings?> = dao.getAppSettings().transformLatest { appSettings ->
        if (appSettings != null) emit(appSettings)
        else Migrator.migrateOrCreateAppSettings(appSettingsController, dao, realmProvider, emailLanguageUtils)
    }

    suspend fun getAppSettings(): AppSettings? = appSettings.first()

    /**
     * Asynchronously sets the application theme.
     *
     * @param theme The new theme to apply.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun setTheme(theme: Theme) {
        dao.updateTheme(theme)
    }

    /**
     * Asynchronously sets the validity period for certain application features.
     *
     * @param validityPeriod The new validity period.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun setValidityPeriod(validityPeriod: ValidityPeriod) {
        dao.updateValidityPeriod(validityPeriod)
    }

    /**
     * Asynchronously sets the download limit for files.
     *
     * @param downloadLimit The new download limit.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun setDownloadLimit(downloadLimit: DownloadLimit) {
        dao.updateDownloadLimit(downloadLimit)
    }

    /**
     * Asynchronously sets the language for email communications.
     *
     * @param emailLanguage The new email language.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun setEmailLanguage(emailLanguage: EmailLanguage) {
        dao.updateEmailLanguage(emailLanguage)
    }

    /**
     * Asynchronously sets the last type of transfer selected by the user.
     *
     * @param transferType The last type of transfer selected.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun setLastTransferType(transferType: TransferType) {
        dao.updateLastTransferType(transferType)
    }

    /**
     * Asynchronously sets the last email of the transfer author entered by the user.
     *
     * @param authorEmail The last email of the transfer author entered.
     *
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(CancellationException::class)
    suspend fun setLastAuthorEmail(authorEmail: String?) {
        dao.updateLastAuthorEmail(authorEmail)
    }

    private object Migrator {
        private val mutex = Mutex()

        suspend fun migrateOrCreateAppSettings(
            appSettingsController: AppSettingsController,
            dao: AppSettingsDao,
            realmProvider: RealmProvider,
            emailLanguageUtils: EmailLanguageUtils
        ) {
            mutex.withLock {
                val hasMigrated = dao.getAppSettings().first() != null
                if (hasMigrated) return
                try {
                    val oldAppSettings = appSettingsController.getAppSettings()?.toNewAppSettingsDB()
                    val settings = oldAppSettings
                        ?: AppSettingsDB(emailLanguage = emailLanguageUtils.getEmailLanguageFromLocale())
                    dao.put(settings)
                    appSettingsController.removeData()
                } finally {
                    realmProvider.closeAppSettingsDb()
                }
            }
        }

        private fun AppSettings.toNewAppSettingsDB(): AppSettingsDB = AppSettingsDB(
            theme = theme,
            validityPeriod = validityPeriod,
            downloadLimit = downloadLimit,
            emailLanguage = emailLanguage,
            lastTransferType = lastTransferType,
            lastAuthorEmail = lastAuthorEmail,
        )
    }
}
