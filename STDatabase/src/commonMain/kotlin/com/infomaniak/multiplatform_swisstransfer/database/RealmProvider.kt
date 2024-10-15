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

import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.AppSettingsDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.ContainerDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.Upload
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class RealmProvider {

    val realmAppSettings by lazy { Realm.open(realmAppSettingsConfiguration) }
    val realmUploads by lazy { Realm.open(realmUploadConfiguration) }
    var realmTransfers: Realm? = null
        private set

    fun openRealmTransfers(userId: Int, inMemory: Boolean = false) {
        realmTransfers = Realm.open(realmTransfersConfiguration(userId, inMemory))
    }

    fun closeRealmAppSettings() {
        realmAppSettings.close()
    }

    fun closeRealmUploads() {
        realmUploads.close()
    }

    fun closeRealmTransfers() {
        realmTransfers?.close()
    }

    fun closeAllRealms() {
        closeRealmAppSettings()
        closeRealmUploads()
        closeRealmTransfers()
    }

    private val realmAppSettingsConfiguration = RealmConfiguration
        .Builder(schema = setOf(AppSettingsDB::class))
        .name("AppSettings")
        .build()

    private val realmUploadConfiguration = RealmConfiguration
        .Builder(schema = setOf(Upload::class))
        .name("Uploads")
        .build()

    private fun realmTransfersConfiguration(userId: Int, inMemory: Boolean) = RealmConfiguration
        .Builder(schema = setOf(TransferDB::class, ContainerDB::class, FileDB::class))
        .name(transferRealmName(userId))
        .apply { if (inMemory) inMemory() }
        .build()

    private fun transferRealmName(userId: Int) = "Transfers-$userId"
}
