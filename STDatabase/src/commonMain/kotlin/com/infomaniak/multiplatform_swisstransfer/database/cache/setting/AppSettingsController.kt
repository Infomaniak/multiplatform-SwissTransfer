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

import com.infomaniak.multiplatform_swisstransfer.database.models.setting.AppSettings
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

class AppSettingsController(private val realm: Realm) {

    //region Get data

    fun getAppSettings(): AppSettings {
        val block: (Realm) -> AppSettings =
            { it.query<AppSettings>().first().find() ?: it.writeBlocking { copyToRealm(AppSettings()) } }
        return realm.let(block)
    }

    //endregion

    //region Edit data
    fun updateAppSettings(onUpdate: (appSettings: AppSettings) -> Unit) {
        realm.writeBlocking { onUpdate(getAppSettings()) }
    }

    fun removeAppSettings() {
        realm.writeBlocking { getAppSettings().let(::delete) }
    }
    //endregion
}
