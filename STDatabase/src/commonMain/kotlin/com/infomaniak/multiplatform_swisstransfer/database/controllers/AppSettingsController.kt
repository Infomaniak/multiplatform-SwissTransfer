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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.CrashReportInterface
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferType
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.AppSettingsDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.RealmUtils.runThrowingRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class AppSettingsController(private val realmProvider: RealmProvider, private val crashReport: CrashReportInterface) {

    private val realm by lazy { realmProvider.appSettings }

    private val appSettingsQuery get() = realm.query<AppSettingsDB>().first()

    @Throws(RealmException::class, CancellationException::class)
    suspend fun initAppSettings(defaultEmailLanguage: EmailLanguage) = runThrowingRealm {
        if (appSettingsQuery.find() == null) {
            realm.write {
                copyToRealm(AppSettingsDB(defaultEmailLanguage), UpdatePolicy.ALL)
            }
        }
    }

    //region Get data
    @Throws(RealmException::class)
    fun getAppSettingsFlow(): Flow<AppSettings?> = runThrowingRealm {
        return appSettingsQuery.asFlow()
            .map { it.obj }
            .catch { exception ->
                crashReport.capture(message = "An error occurred while getting appSettings from Realm DB", exception)
                emit(null)
            }
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
