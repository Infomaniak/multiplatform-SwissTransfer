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

package com.infomaniak.multiplatform_swisstransfer.database

import com.infomaniak.multiplatform_swisstransfer.database.models.ContainerDB
import com.infomaniak.multiplatform_swisstransfer.database.models.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.models.setting.AppSettingsDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadTasks
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class RealmProvider {

    val realmAppSettings by lazy { Realm.open(realmAppSettingsConfiguration) }

    val realmUploadTasks by lazy { Realm.open(realmUploadTasksConfiguration) }

    var realmTransfers: Realm? = null
        private set

    fun loadRealmTransfers(userId: Int) {
        realmTransfers = Realm.open(realmTransfersConfiguration(userId))
    }

    fun closeRealmAppSettings() {
        realmAppSettings.close()
    }

    fun closeRealmUploadTasks() {
        realmUploadTasks.close()
    }

    fun closeCurrentRealmTransfers() {
        realmTransfers?.close()
    }

    fun closeRealm(realm: Realm) {
        realm.close()
    }

    fun closeAllRealms() {
        closeRealmAppSettings()
        closeRealmUploadTasks()
        closeCurrentRealmTransfers()
    }

    private val realmAppSettingsConfiguration = RealmConfiguration
        .Builder(schema = setOf(AppSettingsDB::class))
        .name("AppSettings")
        .build()

    private val realmUploadTasksConfiguration = RealmConfiguration
        .Builder(schema = setOf(UploadTasks::class))
        .name("UploadTasks")
        .build()

    private fun realmTransfersConfiguration(userId: Int) = RealmConfiguration
        .Builder(schema = setOf(TransferDB::class, ContainerDB::class, FileDB::class))
        .name(transferRealmName(userId))
        .build()

    private fun transferRealmName(userId: Int) = "Transfers-$userId"
}
