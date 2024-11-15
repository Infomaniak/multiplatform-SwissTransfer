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

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.*
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.AppSettingsDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.RealmUtils.runThrowingRealm
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

    @Throws(RealmException::class, CancellationException::class)
    suspend fun initAppSettings() = runThrowingRealm {
        if (appSettingsQuery.find() == null) {
            realm.write {
                copyToRealm(AppSettingsDB(), UpdatePolicy.ALL)
            }
        }
    }

    //region Get data
    @Throws(RealmException::class)
    fun getAppSettingsFlow(): Flow<AppSettings?> = runThrowingRealm {
        return appSettingsQuery.asFlow().mapLatest { it.obj }
    }

    @Throws(RealmException::class)
    fun getAppSettings(): AppSettings? = runThrowingRealm {
        return appSettingsQuery.find()
    }
    //endregion

    //region Update data
    @Throws(RealmException::class, CancellationException::class)
    private suspend fun updateAppSettings(onUpdate: (AppSettingsDB) -> Unit) = runThrowingRealm {
        val appSettings = appSettingsQuery.find() ?: return@runThrowingRealm

        realm.write {
            findLatest(appSettings)?.let { mutableAppSettings ->
                onUpdate(mutableAppSettings)
            }
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun setTheme(theme: Theme) = runThrowingRealm {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.theme = theme
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun setValidityPeriod(validityPeriod: ValidityPeriod) = runThrowingRealm {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.validityPeriod = validityPeriod
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun setDownloadLimit(downloadLimit: DownloadLimit) = runThrowingRealm {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.downloadLimit = downloadLimit
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun setEmailLanguage(emailLanguage: EmailLanguage) = runThrowingRealm {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.emailLanguage = emailLanguage
            mutableAppSettings.hasCustomEmailLanguage = true
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun setLastTransferType(transferType: TransferType) = runThrowingRealm {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.lastTransferType = transferType
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun setLastAuthorEmail(authorEmail: String?) = runThrowingRealm {
        updateAppSettings { mutableAppSettings ->
            mutableAppSettings.lastAuthorEmail = authorEmail
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeData() = runThrowingRealm {
        realm.write { deleteAll() }
    }
    //endregion
}
