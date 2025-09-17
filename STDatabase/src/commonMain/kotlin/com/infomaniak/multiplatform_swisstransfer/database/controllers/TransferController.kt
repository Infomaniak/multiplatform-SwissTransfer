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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Container
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.common.utils.DateUtils
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.ContainerDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.DownloadManagerRef
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.FileUtils
import com.infomaniak.multiplatform_swisstransfer.database.utils.findSuspend
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.TypedRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.RealmScalarQuery
import io.realm.kotlin.query.RealmSingleQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.TRUE_PREDICATE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class TransferController(private val realmProvider: RealmProvider) {

    //region Get data
    @Throws(RealmException::class)
    fun getAllTransfersFlow(): Flow<List<Transfer>> = realmProvider.flowWithTransfersDb {
        getAllTransfersFlow(realmProvider)
    }

    @Throws(RealmException::class)
    fun getValidTransfersFlow(transferDirection: TransferDirection): Flow<List<Transfer>> = realmProvider.flowWithTransfersDb {
        getValidTransfersFlow(realmProvider, transferDirection)
    }

    @Throws(RealmException::class)
    fun getExpiredTransfersFlow(transferDirection: TransferDirection): Flow<List<Transfer>> = realmProvider.flowWithTransfersDb {
        getExpiredTransfersFlow(realmProvider, transferDirection)
    }

    @Throws(RealmException::class)
    fun getTransfersCountFlow(transferDirection: TransferDirection): Flow<Long> = realmProvider.flowWithTransfersDb {
        getTransfersCount(realmProvider, transferDirection).asFlow()
    }

    @Throws(RealmException::class)
    fun getTransferFlow(linkUUID: String): Flow<Transfer?> = realmProvider.flowWithTransfersDb { realm ->
        realm.getTransferQuery(linkUUID).asFlow().map { it.obj }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getTransfer(linkUUID: String): Transfer? = realmProvider.withTransfersDb { realm ->
        return realm.getTransferQuery(linkUUID).findSuspend()
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getAllTransfers(): List<Transfer> = realmProvider.withTransfersDb { realm ->
        return realm.getAllTransfersQuery().findSuspend()
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getNotReadyTransfers(): List<Transfer> = realmProvider.withTransfersDb { realm ->
        val query = "${TransferDB.transferStatusPropertyName} != '${TransferStatus.READY.name}'"
        return realm.query<TransferDB>(query).findSuspend()
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getContainers(): List<Container> = realmProvider.withTransfersDb { realm ->
        return realm.query<ContainerDB>().findSuspend()
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getFiles(): List<File> = realmProvider.withTransfersDb { realm ->
        return realm.query<FileDB>().findSuspend()
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getOrphanContainers(): List<Container> = realmProvider.withTransfersDb { realm ->
        return realm.getOrphanContainers().findSuspend()
    }
    //endregion

    //region Upsert data
    @Throws(RealmException::class, CancellationException::class, TransferWithoutFilesException::class)
    suspend fun insert(
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
    @Throws(RealmException::class, CancellationException::class, TransferWithoutFilesException::class)
    suspend fun update(transfer: Transfer) = realmProvider.withTransfersDb { realm ->
        realm.write {
            getTransferQuery(transfer.linkUUID).find()?.let {
                it.downloadCounterCredit = transfer.downloadCounterCredit
                it.isMailSent = transfer.isMailSent
                it.downloadHost = transfer.downloadHost
                it.transferStatus = if (transfer.downloadCounterCredit <= 0) {
                    TransferStatus.EXPIRED_DOWNLOAD_QUOTA
                } else {
                    transfer.transferStatus ?: TransferStatus.READY
                }
            }
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun updateTransferStatus(transferUUID: String, transferStatus: TransferStatus) =
        realmProvider.withTransfersDb { realm ->
            realm.write {
                val transferToUpdate = this.getTransferQuery(transferUUID).find()
                transferToUpdate?.transferStatus = transferStatus
            }
        }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun updateTransferFilesThumbnails(transferUUID: String, thumbnailRootPath: String) =
        realmProvider.withTransfersDb { realm ->
            realm.write {
                val transferToUpdate = this.getTransferQuery(transferUUID).find()
                transferToUpdate?.container?.files?.forEach {
                    it.thumbnailPath = "$thumbnailRootPath/${it.uuid}"
                }
            }
        }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun deleteTransfer(transferUUID: String) = realmProvider.withTransfersDb { realm ->
        realm.write {
            getTransferQuery(transferUUID).find()?.let { transferToDelete ->
                delete(this.getDownloadManagerIdsQuery(transferUUID))

                deleteContainerAndFiles(transferToDelete)
                delete(transferToDelete)
            }
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun deleteExpiredTransfers() = realmProvider.withTransfersDb { realm ->
        realm.write {
            val expiredTransfersQuery = getExpiredTransfersQuery()

            expiredTransfersQuery.find().forEach { expiredTransfer ->
                delete(this.getDownloadManagerIdsQuery(expiredTransfer.linkUUID))

                deleteContainerAndFiles(expiredTransfer)
            }

            delete(expiredTransfersQuery)

            deleteOrphanContainers()
        }
    }

    private fun MutableRealm.deleteOrphanContainers() {
        getOrphanContainers().find().forEach { container ->
            delete(container.files)
            delete(container)
        }
    }

    private fun MutableRealm.deleteContainerAndFiles(transferToDelete: TransferDB) {
        transferToDelete.container?.let { containerToDelete ->
            delete(containerToDelete.files)
            delete(containerToDelete)
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeData() = realmProvider.withTransfersDb { realm ->
        realm.write { deleteAll() }
    }
    //endregion

    //region DownloadManager ID
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

    fun downloadManagerIdFor(transferUUID: String, fileUid: String?): Flow<Long?> = realmProvider.flowWithTransfersDb { realm ->
        getDownloadManagerIdQuery(realm, transferUUID, fileUid).asFlow().map { it.obj?.downloadManagerUniqueId }
    }
    //endregion

    private companion object {

        private const val DAYS_SINCE_EXPIRATION = 15

        @OptIn(ExperimentalTime::class)
        private val expiredDateQuery = "${TransferDB::expiredDateTimestamp.name} < ${Clock.System.now().epochSeconds}"
        private val downloadCounterQuery = "${TransferDB::downloadCounterCredit.name} <= 0"

        private fun getAllTransfersQuery(realm: Realm): RealmQuery<TransferDB> {
            return realm.query<TransferDB>().sort(TransferDB::createdDateTimestamp.name, Sort.DESCENDING)
        }

        private fun getValidTransfersQuery(realm: Realm, transferDirection: TransferDirection?): RealmQuery<TransferDB> {
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
        private suspend fun getAllTransfersFlow(
            realmProvider: RealmProvider,
        ): Flow<List<TransferDB>> = realmProvider.withTransfersDb { realm ->
            return@withTransfersDb getAllTransfersQuery(realm).asFlow().map { it.list }
        }

        @Throws(RealmException::class, CancellationException::class)
        private suspend fun getValidTransfersFlow(
            realmProvider: RealmProvider,
            transferDirection: TransferDirection,
        ): Flow<List<TransferDB>> = realmProvider.withTransfersDb { realm ->
            return@withTransfersDb getValidTransfersQuery(realm, transferDirection).asFlow().map { it.list }
        }

        @Throws(RealmException::class, CancellationException::class)
        private suspend fun getExpiredTransfersFlow(
            realmProvider: RealmProvider,
            transferDirection: TransferDirection,
        ): Flow<List<TransferDB>> = realmProvider.withTransfersDb { realm ->
            return@withTransfersDb getExpiredTransfersQuery(realm, transferDirection).asFlow().map { it.list }
        }

        @Throws(RealmException::class, CancellationException::class)
        private suspend fun getTransfersCount(
            realmProvider: RealmProvider,
            transferDirection: TransferDirection?,
        ): RealmScalarQuery<Long> = realmProvider.withTransfersDb { realm ->
            return realm.query<TransferDB>(directionFilterQuery(transferDirection)).count()
        }

        private fun TypedRealm.getDownloadManagerIdsQuery(transferUUID: String): RealmResults<DownloadManagerRef> {
            return query<DownloadManagerRef>("${DownloadManagerRef::transferUUID.name} == '$transferUUID'").find()
        }

        private fun getDownloadManagerIdQuery(
            realm: TypedRealm,
            transferUUID: String,
            fileUid: String?
        ): RealmSingleQuery<DownloadManagerRef> {
            val id = DownloadManagerRef.buildPrimaryKeyForRealm(transferUUID, fileUid)
            return realm.query<DownloadManagerRef>("${DownloadManagerRef::id.name} == '$id'").first()
        }

        private fun TypedRealm.getTransferQuery(linkUUID: String): RealmSingleQuery<TransferDB> {
            return query<TransferDB>("${TransferDB::linkUUID.name} == '$linkUUID'").first()
        }

        private fun TypedRealm.getAllTransfersQuery(): RealmQuery<TransferDB> = query<TransferDB>()

        private fun TypedRealm.getExpiredTransfersQuery(): RealmQuery<TransferDB> {
            @OptIn(ExperimentalTime::class)
            val expiry = Clock.System.now().epochSeconds - (DAYS_SINCE_EXPIRATION * DateUtils.SECONDS_IN_A_DAY)
            return query<TransferDB>("${TransferDB::expiredDateTimestamp.name} < '${expiry}'")
        }

        private fun TypedRealm.getOrphanContainers(): RealmQuery<ContainerDB> {
            return query<ContainerDB>("${ContainerDB::transfer.name}.@count == 0")
        }
    }
}
