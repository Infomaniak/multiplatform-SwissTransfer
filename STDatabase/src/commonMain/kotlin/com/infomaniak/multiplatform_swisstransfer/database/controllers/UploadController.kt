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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.Upload
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.upload.UploadDB
import io.realm.kotlin.ext.query
import kotlin.coroutines.cancellation.CancellationException

class UploadController(private val realmProvider: RealmProvider) {

    private val realm by lazy { realmProvider.realmUploads }

    //region Queries
    private fun getUploadsQuery() = realm.query<UploadDB>()
    //endregion

    //region Get data
    @Throws(IllegalArgumentException::class, CancellationException::class)
    fun getUploads(): List<Upload> = getUploadsQuery().find()

    @Throws(IllegalArgumentException::class)
    fun getUploadByUuid(uuid: String): Upload? {
        return realm.query<UploadDB>("${UploadDB::uuid.name} == '$uuid'").first().find()
    }

    @Throws(IllegalArgumentException::class, CancellationException::class)
    fun getUploadsCount(): Long = getUploadsQuery().count().find()
    //endregion

    //region Insert
    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun insert(upload: Upload) {
        realm.write {
            this.copyToRealm(UploadDB(upload))
        }
    }
    //endregion

    //region Update data
    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun removeData() {
        realm.write { deleteAll() }
    }
    //endregion
}
