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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.findSuspend
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FileController(private val realmProvider: RealmProvider) {

    @Throws(RealmException::class)
    fun getFilesFromTransfer(folderUuid: String): Flow<List<File>> = realmProvider.flowWithTransfersDb { realm ->
        val query = "${FileDB::folder.name}.uuid == '$folderUuid'"
        realm.query<FileDB>(query).asFlow().map { it.list }
    }

    suspend fun getFile(fileUUID: String): File? = realmProvider.withTransfersDb { realm ->
        val query = "${FileDB::uuid.name} == '$fileUUID'"
        return realm.query<FileDB>(query).first().findSuspend()
    }
}
