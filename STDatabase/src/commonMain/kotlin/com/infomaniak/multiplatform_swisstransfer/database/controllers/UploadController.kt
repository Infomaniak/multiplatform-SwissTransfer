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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadContainer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadContainerDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadFileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadSessionDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.RealmUtils.runThrowingRealm
import io.realm.kotlin.TypedRealm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.RealmSingleQuery
import kotlin.coroutines.cancellation.CancellationException

class UploadController(private val realmProvider: RealmProvider) {

    private val realm by lazy { realmProvider.realmUploads }

    //region Queries
    private fun getUploadsQuery() = realm.query<UploadSessionDB>()
    //endregion

    //region Get data
    @Throws(RealmException::class)
    fun getAllUploads(): List<UploadSessionDB> = runThrowingRealm {
        getUploadsQuery().find()
    }

    @Throws(RealmException::class)
    fun getUploads(hasBeenInitialized: Boolean): List<UploadSessionDB> = runThrowingRealm {
        getUploadsQuery().find().filter {
            if (hasBeenInitialized) it.remoteContainer != null else it.remoteContainer == null
        }
    }

    @Throws(RealmException::class)
    fun getUploadByUuid(uuid: String): UploadSession? = runThrowingRealm {
        return realm.query<UploadSessionDB>("${UploadSessionDB::uuid.name} == '$uuid'").first().find()
    }

    @Throws(RealmException::class)
    fun getUploadsCount(): Long = runThrowingRealm {
        getUploadsQuery().count().find()
    }
    //endregion

    //region Insert
    @Throws(RealmException::class, CancellationException::class)
    suspend fun insert(uploadSession: UploadSession) = runThrowingRealm {
        realm.write {
            this.copyToRealm(UploadSessionDB(uploadSession))
        }
    }
    //endregion

    //region Update data
    @Throws(RealmException::class, CancellationException::class)
    suspend fun updateUploadSession(
        uuid: String,
        remoteContainer: UploadContainer,
        remoteUploadHost: String,
        remoteFilesUuid: List<String>,
    ) = runThrowingRealm {
        realm.write {
            getUploadSessionQuery(uuid).find()?.apply {
                this.remoteContainer = UploadContainerDB(remoteContainer)
                this.remoteUploadHost = remoteUploadHost
                this.files.forEachIndexed { index, file ->
                    file.remoteUploadFile = UploadFileDB(remoteFilesUuid[index])
                }
            }
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeData() = runThrowingRealm {
        realm.write { deleteAll() }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeUploadSession(uuid: String) = runThrowingRealm {
        realm.write {
            val finishedUploadSession = getUploadSessionResult(uuid)
            delete(finishedUploadSession)
        }
    }
    //endregion

    private companion object {
        //region Query
        private fun TypedRealm.getUploadSessionQuery(uuid: String): RealmSingleQuery<UploadSessionDB> {
            return query<UploadSessionDB>("${UploadSessionDB::uuid.name} == '$uuid'").first()
        }
        //endregion

        //region Result
        private fun TypedRealm.getUploadSessionResult(uuid: String): RealmResults<UploadSessionDB> {
            return query<UploadSessionDB>("${UploadSessionDB::uuid.name} == '$uuid'").find()
        }
        //endregion
    }
}
