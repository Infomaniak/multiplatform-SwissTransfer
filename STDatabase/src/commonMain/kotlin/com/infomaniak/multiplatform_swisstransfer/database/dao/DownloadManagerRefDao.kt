/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.DownloadManagerRef
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadManagerRefDao {

    @Query("SELECT downloadManagerUniqueId FROM DownloadManagerRef WHERE transferId=:transferId AND fileId=:fileId")
    fun getDownloadManagerId(transferId: String, fileId: String = ""): Flow<Long?>

    @Upsert
    suspend fun update(downloadManagerRef: DownloadManagerRef)

    @Query("DELETE FROM DownloadManagerRef WHERE transferId=:transferId AND fileId=:fileId")
    suspend fun delete(transferId: String, fileId: String = "")

    @Query("DELETE FROM DownloadManagerRef WHERE userOwnerId=:userId")
    suspend fun deleteAll(userId: Long)

}
