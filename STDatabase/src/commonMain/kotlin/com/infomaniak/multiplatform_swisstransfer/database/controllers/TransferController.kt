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
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.DownloadManagerRef
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtils
import com.infomaniak.multiplatform_swisstransfer.database.utils.findSuspend
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.TypedRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.coroutines.cancellation.CancellationException

class TransferController(private val realmProvider: RealmProvider) {

    //region Get data
    @Throws(RealmException::class, CancellationException::class)
    internal suspend fun getTransfers(
        transferDirection: TransferDirection? = null
    ): List<TransferDB> = getTransfers(realmProvider, transferDirection)

    @Throws(RealmException::class)
    fun getTransfersFlow(transferDirection: TransferDirection): Flow<List<Transfer>> = realmProvider.flowWithTransfersDb {
        getTransfersFlow(realmProvider, transferDirection)
    }

    @Throws(RealmException::class)
    fun getTransfersCountFlow(transferDirection: TransferDirection): Flow<Long> = realmProvider.flowWithTransfersDb {
        getTransfersCount(realmProvider, transferDirection).asFlow()
    }

    @Throws(RealmException::class)
    fun getTransferFlow(linkUUID: String): Flow<Transfer?> = realmProvider.flowWithTransfersDb { realm ->
        getTransferQuery(realm, linkUUID).asFlow().map { it.obj }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getTransfer(linkUUID: String): Transfer? = realmProvider.withTransfersDb { realm ->
        return getTransferQuery(realm, linkUUID).findSuspend()
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getNotReadyTransfers(): List<Transfer> = realmProvider.withTransfersDb { realm ->
        val query = "${TransferDB.transferStatusPropertyName} != '${TransferStatus.READY.name}'"
        return realm.query<TransferDB>(query).findSuspend()
    }
    //endregion

    //region Upsert data
    @Throws(RealmException::class, CancellationException::class, TransferWithoutFilesException::class)
    suspend fun upsert(
        transfer: Transfer,
        transferDirection: TransferDirection,
        password: String?,
        recipientsEmails: Set<String> = emptySet(),
    ) = realmProvider.withTransfersDb { realm ->
        realm.write {
            val transferDB = TransferDB(transfer, transferDirection, password, recipientsEmails)
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
            delete(getDownloadManagerIdsQuery(this, transferUUID))
            delete(transferToDelete)
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun deleteExpiredTransfers() = realmProvider.withTransfersDb { realm ->
        realm.write {
            val expiredTransfersQuery = getExpiredTransfersQuery(realm = this)
            expiredTransfersQuery.find().forEach { delete(getDownloadManagerIdsQuery(this, it.linkUUID)) }
            delete(expiredTransfersQuery)
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeData() = realmProvider.withTransfersDb { realm ->
        realm.write { deleteAll() }
    }

    //endregion

    //region DownloadManager id

    suspend fun writeDownloadManagerId(
        transferUUID: String,
        fileUid: String?,
        uniqueDownloadManagerId: Long?
    ) {
        realmProvider.withTransfersDb { realm ->
            realm.write {
                if (uniqueDownloadManagerId == null) delete(
                    getDownloadManagerIdQuery(
                        realm = this,
                        transferUUID = transferUUID,
                        fileUid = fileUid
                    )
                ) else copyToRealm(
                    instance = DownloadManagerRef(
                        transferUUID = transferUUID,
                        fileUid = fileUid,
                        downloadManagerUniqueId = uniqueDownloadManagerId
                    ),
                    updatePolicy = UpdatePolicy.ALL
                )
            }
        }
    }

    suspend fun readDownloadManagerId(transferUUID: String, fileUid: String?): Long? {
        return realmProvider.withTransfersDb { realm ->
            getDownloadManagerIdQuery(realm, transferUUID, fileUid).findSuspend()?.downloadManagerUniqueId
        }
    }

    fun downloadManagerIdFor(transferUUID: String, fileUid: String?): Flow<Long?> = realmProvider.flowWithTransfersDb { realm ->
        getDownloadManagerIdQuery(realm, transferUUID, fileUid).asFlow().map { it.obj?.downloadManagerUniqueId }
    }

    //endregion

    private companion object {

        private const val DAYS_SINCE_EXPIRATION = 15

        private val expiredDateQuery = "${TransferDB::expiredDateTimestamp.name} < ${Clock.System.now().epochSeconds}"
        private val downloadCounterQuery = "${TransferDB::downloadCounterCredit.name} <= 0"

        private fun getNormalTransfersQuery(realm: Realm, transferDirection: TransferDirection?): RealmQuery<TransferDB> {
            val directionFilterQuery = directionFilterQuery(transferDirection)
            return realm.query<TransferDB>("$directionFilterQuery AND NOT ($expiredDateQuery OR $downloadCounterQuery)")
                .sort(TransferDB::createdDateTimestamp.name, Sort.DESCENDING)
        }

        private fun getExpiredTransfersQuery(realm: Realm, transferDirection: TransferDirection?): RealmQuery<TransferDB> {
            val directionFilterQuery = directionFilterQuery(transferDirection)
            return realm.query<TransferDB>("$directionFilterQuery AND ($expiredDateQuery OR $downloadCounterQuery)")
                .sort(TransferDB::createdDateTimestamp.name, Sort.DESCENDING)
        }

        private fun directionFilterQuery(transferDirection: TransferDirection?): String {
            return when (transferDirection) {
                null -> TRUE_PREDICATE
                else -> "${TransferDB.transferDirectionPropertyName} == '${transferDirection}'"
            }
        }

        @Throws(RealmException::class, CancellationException::class)
        private suspend fun getTransfers(
            realmProvider: RealmProvider,
            transferDirection: TransferDirection?,
        ): List<TransferDB> = realmProvider.withTransfersDb { realm ->

            val normalTransfers = getNormalTransfersQuery(realm, transferDirection).findSuspend()
            val expiredTransfers = getExpiredTransfersQuery(realm, transferDirection).findSuspend()

            return@withTransfersDb normalTransfers + expiredTransfers
        }

        @Throws(RealmException::class, CancellationException::class)
        private suspend fun getTransfersFlow(
            realmProvider: RealmProvider,
            transferDirection: TransferDirection?,
        ): Flow<List<TransferDB>> = realmProvider.withTransfersDb { realm ->

            val normalTransfers = getNormalTransfersQuery(realm, transferDirection).asFlow()
            val expiredTransfers = getExpiredTransfersQuery(realm, transferDirection).asFlow()

            return@withTransfersDb normalTransfers.combine(expiredTransfers) { normal, expired ->
                normal.list + expired.list
            }
        }

        @Throws(RealmException::class, CancellationException::class)
        private suspend fun getTransfersCount(
            realmProvider: RealmProvider,
            transferDirection: TransferDirection?,
        ): RealmScalarQuery<Long> = realmProvider.withTransfersDb { realm ->
            return realm.query<TransferDB>(directionFilterQuery(transferDirection)).count()
        }

        private fun getDownloadManagerIdsQuery(
            realm: TypedRealm,
            transferUUID: String
        ): RealmResults<DownloadManagerRef> {
            return realm.query<DownloadManagerRef>("${DownloadManagerRef::transferUUID.name} == '$transferUUID'").find()
        }

        private fun getDownloadManagerIdQuery(
            realm: TypedRealm,
            transferUUID: String,
            fileUid: String?
        ): RealmSingleQuery<DownloadManagerRef> {
            val id = DownloadManagerRef.buildPrimaryKeyForRealm(transferUUID, fileUid)
            return realm.query<DownloadManagerRef>("${DownloadManagerRef::id.name} == '$id'").first()
        }

        private fun getTransferQuery(realm: Realm, linkUUID: String): RealmSingleQuery<TransferDB> {
            return realm.query<TransferDB>("${TransferDB::linkUUID.name} == '$linkUUID'").first()
        }

        private fun getExpiredTransfersQuery(realm: MutableRealm): RealmQuery<TransferDB> {
            val expiry = Clock.System.now().epochSeconds - (DAYS_SINCE_EXPIRATION * DateUtils.SECONDS_IN_A_DAY)
            return realm.query<TransferDB>("${TransferDB::expiredDateTimestamp.name} < '${expiry}'")
        }
    }
}
