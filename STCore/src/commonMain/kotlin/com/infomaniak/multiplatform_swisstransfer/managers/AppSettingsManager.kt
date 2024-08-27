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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import com.infomaniak.multiplatform_swisstransfer.database.cache.setting.AppSettingsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AppSettingsManager internal constructor(
    private val appSettingsController: AppSettingsController,
) {
    val appSettings: Flow<AppSettings?>
        get() = appSettingsController.getAppSettingsFlow().flowOn(Dispatchers.IO)

    fun getTheme() = appSettingsController.getTheme()

    suspend fun setTheme(theme: Theme) = withContext(Dispatchers.IO) {
        appSettingsController.setTheme(theme)
    }

    fun getValidityPeriod() = appSettingsController.getValidityPeriod()

    suspend fun setValidityPeriod(validityPeriod: ValidityPeriod) = withContext(Dispatchers.IO) {
        appSettingsController.setValidityPeriod(validityPeriod)
    }

    fun getDownloadLimit() = appSettingsController.getDownloadsLimit()

    suspend fun setDownloadLimit(downloadLimit: DownloadLimit) = withContext(Dispatchers.IO) {
        appSettingsController.setDownloadLimit(downloadLimit)
    }

    fun getEmailLanguage() = appSettingsController.getEmailLanguage()

    suspend fun setEmailLanguage(emailLanguage: EmailLanguage) = withContext(Dispatchers.IO) {
        appSettingsController.setEmailLanguage(emailLanguage)
    }
}
