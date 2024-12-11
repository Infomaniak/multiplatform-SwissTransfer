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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.*
import com.infomaniak.multiplatform_swisstransfer.database.controllers.AppSettingsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Provides a convenient interface for managing application settings, abstracting the underlying
 * [AppSettingsController]. It handles asynchronous operations and ensures settings updates are
 * performed on a background thread.
 **/
class AppSettingsManager internal constructor(
    private val appSettingsController: AppSettingsController,
) {

    /**
     * A [Flow] that emits the current [AppSettings] object whenever it changes. This flow operates
     * on the [Dispatchers.IO] context to avoid blocking the main thread.
     */
    val appSettings: Flow<AppSettings?>
        get() = appSettingsController.getAppSettingsFlow().flowOn(Dispatchers.IO)

    fun getAppSettings(): AppSettings? = appSettingsController.getAppSettings()

    /**
     * Asynchronously sets the application theme.
     *
     * @param theme The new theme to apply.
     *
     * @throws RealmException If the provided theme is invalid.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setTheme(theme: Theme): Unit = withContext(Dispatchers.IO) {
        appSettingsController.setTheme(theme)
    }

    /**
     * Asynchronously sets the validity period for certain application features.
     *
     * @param validityPeriod The new validity period.
     *
     * @throws RealmException If the provided validity period is invalid.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setValidityPeriod(validityPeriod: ValidityPeriod): Unit = withContext(Dispatchers.IO) {
        appSettingsController.setValidityPeriod(validityPeriod)
    }

    /**
     * Asynchronously sets the download limit for files.
     *
     * @param downloadLimit The new download limit.
     *
     * @throws RealmException If the provided download limit is invalid.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setDownloadLimit(downloadLimit: DownloadLimit): Unit = withContext(Dispatchers.IO) {
        appSettingsController.setDownloadLimit(downloadLimit)
    }

    /**
     * Asynchronously sets the language for email communications.
     *
     * @param emailLanguage The new email language.
     *
     * @throws RealmException If the provided email language is invalid.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setEmailLanguage(emailLanguage: EmailLanguage): Unit = withContext(Dispatchers.IO) {
        appSettingsController.setEmailLanguage(emailLanguage)
    }

    /**
     * Asynchronously sets the last type of transfer selected by the user.
     *
     * @param transferType The last type of transfer selected.
     *
     * @throws RealmException If the provided type of transfer is invalid.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setLastTransferType(transferType: TransferType): Unit = withContext(Dispatchers.IO) {
        appSettingsController.setLastTransferType(transferType)
    }

    /**
     * Asynchronously sets the last email of the transfer author entered by the user.
     *
     * @param authorEmail The last email of the transfer author entered.
     *
     * @throws RealmException If the provided email is invalid.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setLastAuthorEmail(authorEmail: String?): Unit = withContext(Dispatchers.IO) {
        appSettingsController.setLastAuthorEmail(authorEmail)
    }

    /**
     * Asynchronously sets the email and it's corresponding token.
     *
     * @param email The validated email.
     * @param token The valid token associated to the email.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setEmailToken(email: String, token: String): Unit = withContext(Dispatchers.IO) {
        appSettingsController.setEmailToken(email, token)
    }

    /**
     * Get a token for a given email.
     *
     * @param email The email possibly associated with a token.
     *
     * @return A token which may be associated with the email.
     */
    fun getTokenForEmail(email: String): String? {
        return appSettingsController.getEmailTokenForEmail(email)?.token
    }
}
