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
import com.infomaniak.multiplatform_swisstransfer.common.exceptions.TransferWithoutFilesException
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.common.utils.DateUtils
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtils
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class TransferController(private val realmProvider: RealmProvider) {

    //region Get data
    @Throws(RealmException::class, CancellationException::class)
    internal suspend fun getTransfers(
        transferDirection: TransferDirection? = null
    ): RealmResults<TransferDB> = getTransfersQuery(realmProvider, transferDirection).find()

    @Throws(RealmException::class)
    fun getTransfersFlow(transferDirection: TransferDirection): Flow<List<Transfer>> = realmProvider.flowWithTransfersDb {
        getTransfers(transferDirection).asFlow().mapLatest { it.list }
    }

    @Throws(RealmException::class)
    fun getTransfersCountFlow(transferDirection: TransferDirection): Flow<Long> = realmProvider.flowWithTransfersDb {
        getTransfersQuery(realmProvider, transferDirection).count().asFlow()
    }

    @Throws(RealmException::class)
    fun getTransferFlow(linkUUID: String): Flow<Transfer?> = realmProvider.flowWithTransfersDb { realm ->
        getTransferQuery(realm, linkUUID).asFlow().mapLatest { it.obj }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getTransfer(linkUUID: String): Transfer? = realmProvider.withTransfersDb { realm ->
        return getTransferQuery(realm, linkUUID).find()
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getNotReadyTransfers(): List<Transfer> = realmProvider.withTransfersDb { realm ->
        val query = "${TransferDB.transferStatusPropertyName} != '${TransferStatus.READY.name}'"
        return realm.query<TransferDB>(query).find()
    }
    //endregion

    //region Upsert data
    @Throws(RealmException::class, CancellationException::class, TransferWithoutFilesException::class)
    suspend fun upsert(transfer: Transfer, transferDirection: TransferDirection, password: String?) =
        realmProvider.withTransfersDb { realm ->
            realm.write {
                val transferDB = TransferDB(transfer, transferDirection, password)
                transferDB.container?.files?.let { transferFiles ->
                    transferDB.container?.files = FileUtils.getFileDBTree(transferDB.containerUUID, transferFiles).toRealmList()
                    this.copyToRealm(transferDB, UpdatePolicy.ALL)
                } ?: throw TransferWithoutFilesException()
            }
        }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun generateAndInsert(
        linkUUID: String,
        uploadSession: UploadSession,
        transferStatus: TransferStatus,
    ) = realmProvider.withTransfersDb { realm ->
        val transferDB = TransferDB(linkUUID, uploadSession, transferStatus).apply {
            container?.files?.let { files ->
                FileUtils.getFileDBTree(containerUUID, files)
            }
        }

        realm.write {
            this.copyToRealm(transferDB, UpdatePolicy.ALL)
        }
    }
    //endregion

    //region Update data
    @Throws(RealmException::class, CancellationException::class)
    suspend fun deleteTransfer(transferUUID: String) = realmProvider.withTransfersDb { realm ->
        realm.write {
            val transferToDelete = query<TransferDB>("${TransferDB::linkUUID.name} == '$transferUUID'").first()
            delete(transferToDelete)
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun deleteExpiredTransfers() = realmProvider.withTransfersDb { realm ->
        realm.write { delete(getExpiredTransfersQuery(realm = this)) }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeData() = realmProvider.withTransfersDb { realm ->
        realm.write { deleteAll() }
    }
    //endregion

    private companion object {

        private const val DAYS_SINCE_EXPIRATION = 15

        private fun getTransferQuery(realm: Realm, linkUUID: String): RealmSingleQuery<TransferDB> {
            return realm.query<TransferDB>("${TransferDB::linkUUID.name} == '$linkUUID'").first()
        }

        private fun getExpiredTransfersQuery(realm: MutableRealm): RealmQuery<TransferDB> {
            val expiry = Clock.System.now().epochSeconds - (DAYS_SINCE_EXPIRATION * DateUtils.SECONDS_IN_A_DAY)
            return realm.query<TransferDB>("${TransferDB::expiredDateTimestamp.name} < '${expiry}'")
        }

        @Throws(RealmException::class, CancellationException::class)
        suspend fun getTransfersQuery(
            realm: RealmProvider,
            transferDirection: TransferDirection?,
        ): RealmQuery<TransferDB> = realm.withTransfersDb { realm ->
            val directionFilterQuery = when (transferDirection) {
                null -> TRUE_PREDICATE
                else -> "${TransferDB.transferDirectionPropertyName} == '${transferDirection}'"
            }
            return realm.query<TransferDB>(directionFilterQuery).sort(TransferDB::createdDateTimestamp.name, Sort.DESCENDING)
        }
    }
}
