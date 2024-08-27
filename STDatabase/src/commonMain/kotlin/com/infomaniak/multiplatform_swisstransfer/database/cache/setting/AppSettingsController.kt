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

package com.infomaniak.multiplatform_swisstransfer.database.cache.setting

import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.setting.AppSettingsDB
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsController(private val realmProvider: RealmProvider) {

    private val realm by lazy { realmProvider.realmAppSettings }

    private val appSettingsQuery
        get() = realm.query<AppSettingsDB>().first()

    suspend fun initAppSettings() {
        if (appSettingsQuery.find() == null) {
            realm.write {
                copyToRealm(AppSettingsDB(), UpdatePolicy.ALL)
            }
        }
    }

    //region Get data

    fun getAppSettingsFlow(): Flow<AppSettingsDB?> {
        return appSettingsQuery.asFlow().mapLatest { it.obj }
    }

    fun getTheme(): Flow<Theme> {
        return appSettingsQuery.asFlow().mapLatest { it.obj?.theme ?: Theme.SYSTEM }
    }

    fun getValidityPeriod(): Flow<ValidityPeriod> {
        return appSettingsQuery.asFlow().mapLatest { it.obj?.validityPeriod ?: ValidityPeriod.THIRTY }
    }

    fun getDownloadsLimit(): Flow<DownloadLimit> {
        return appSettingsQuery.asFlow().mapLatest { it.obj?.downloadLimit ?: DownloadLimit.TWOHUNDREDFIFTY}
    }

    fun getEmailLanguage(): Flow<EmailLanguage> {
        return appSettingsQuery.asFlow().mapLatest { it.obj?.emailLanguage ?: EmailLanguage.FRENCH }
    }

    //endregion

    //region Edit data
    suspend fun setTheme(theme: Theme) {
        val appSettings = appSettingsQuery.find() ?: return

        realm.write {
            findLatest(appSettings)?.let { mutableAppSettings ->
                mutableAppSettings.theme = theme
            }
        }
    }

    suspend fun setValidityPeriod(validityPeriod: ValidityPeriod) {
        val appSettings = appSettingsQuery.find() ?: return

        realm.write {
            findLatest(appSettings)?.let { mutableAppSettings ->
                mutableAppSettings.validityPeriod = validityPeriod
            }
        }
    }

    suspend fun setDownloadLimit(downloadLimit: DownloadLimit) {
        val appSettings = appSettingsQuery.find() ?: return

        realm.write {
            findLatest(appSettings)?.let { mutableAppSettings ->
                mutableAppSettings.downloadLimit = downloadLimit
            }
        }
    }

    suspend fun setEmailLanguage(emailLanguage: EmailLanguage) {
        val appSettings = appSettingsQuery.find() ?: return

        realm.write {
            findLatest(appSettings)?.let { mutableAppSettings ->
                mutableAppSettings.emailLanguage = emailLanguage
            }
        }
    }
    //endregion
}
