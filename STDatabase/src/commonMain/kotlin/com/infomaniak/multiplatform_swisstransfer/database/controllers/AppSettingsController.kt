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
package com.infomaniak.multiplatform_swisstransfer.database.controllers

import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.AppSettingsDB
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsController(private val realmProvider: RealmProvider) {

    private val realm by lazy { realmProvider.realmAppSettings }

    private val appSettingsQuery get() = realm.query<AppSettingsDB>().first()

    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun initAppSettings() {
        if (appSettingsQuery.find() == null) {
            realm.write {
                copyToRealm(AppSettingsDB(), UpdatePolicy.ALL)
            }
        }
    }

    //region Get data
    @Throws(IllegalArgumentException::class, CancellationException::class)
    fun getAppSettingsFlow(): Flow<AppSettingsDB?> {
        return appSettingsQuery.asFlow().mapLatest { it.obj }
    }
    //endregion

    //region Update data
    @Throws(IllegalArgumentException::class, CancellationException::class)
    private suspend fun updateAppSettings(onUpdate: (AppSettingsDB) -> Unit) {
        val appSettings = appSettingsQuery.find() ?: return

        realm.write {
            findLatest(appSettings)?.let { mutableAppSettings ->
                onUpdate(mutableAppSettings)
            }
        }
    }

    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun setTheme(theme: Theme) {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.theme = theme
        }
    }

    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun setValidityPeriod(validityPeriod: ValidityPeriod) {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.validityPeriod = validityPeriod
        }
    }

    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun setDownloadLimit(downloadLimit: DownloadLimit) {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.downloadLimit = downloadLimit
        }
    }

    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun setEmailLanguage(emailLanguage: EmailLanguage) {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.emailLanguage = emailLanguage
        }
    }

    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun removeData() {
        realm.write { deleteAll() }
    }
    //endregion
}
