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
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.EmailTokenDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.ContainerDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.RemoteUploadFileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadContainerDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadFileSessionDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadSessionDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.RealmUtils.runThrowingRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class RealmProvider(private val loadDataInMemory: Boolean = false) {

    val appSettings by lazy { Realm.open(realmAppSettingsConfiguration) }
    val uploads by lazy { Realm.open(realmUploadDBConfiguration) }
    private val transfersAsync = CompletableDeferred<Realm>()
    private suspend fun transfers(): Realm = transfersAsync.await()

    fun openTransfersDb(userId: Int) {
        transfersAsync.complete(Realm.open(realmTransfersConfiguration(userId)))
    }

    internal suspend inline fun <T> withTransfersDb(block: (Realm) -> T): T {
        runThrowingRealm {
            return block(transfers())
        }
    }

    internal fun <T> flowWithTransfersDb(block: suspend (Realm) -> Flow<T>): Flow<T> = flow {
        runThrowingRealm {
            emitAll(block(transfers()))
        }
    }

    fun closeAppSettingsDb() {
        appSettings.close()
    }

    fun closeUploadsDb() {
        uploads.close()
    }

    suspend fun closeTransfersDb() {
        transfersAsync.await().close()
    }

    suspend fun closeAllDatabases() {
        closeAppSettingsDb()
        closeUploadsDb()
        closeTransfersDb()
    }

    private val realmAppSettingsConfiguration = RealmConfiguration
        .Builder(schema = setOf(AppSettingsDB::class, EmailTokenDB::class))
        .name("AppSettings.realm")
        .deleteRealmDataIfNeeded()
        .loadDataInMemoryIfNeeded()
        .build()

    private val realmUploadDBConfiguration = RealmConfiguration
        .Builder(
            schema = setOf(
                UploadSessionDB::class,
                UploadFileSessionDB::class,
                UploadContainerDB::class,
                RemoteUploadFileDB::class,
            )
        )
        .name("Uploads.realm")
        .deleteRealmDataIfNeeded()
        .loadDataInMemoryIfNeeded()
        .build()

    private fun realmTransfersConfiguration(userId: Int) = RealmConfiguration
        .Builder(schema = setOf(TransferDB::class, ContainerDB::class, FileDB::class))
        .name(transferRealmName(userId))
        .deleteRealmDataIfNeeded()
        .loadDataInMemoryIfNeeded()
        .build()

    private fun transferRealmName(userId: Int) = "Transfers-$userId.realm"

    private fun RealmConfiguration.Builder.loadDataInMemoryIfNeeded(): RealmConfiguration.Builder {
        return apply { if (loadDataInMemory) inMemory() }
    }

    // TODO: Remove before going to production !
    private fun RealmConfiguration.Builder.deleteRealmDataIfNeeded(): RealmConfiguration.Builder {
        return apply { if (!loadDataInMemory) deleteRealmIfMigrationNeeded() }
    }
}
