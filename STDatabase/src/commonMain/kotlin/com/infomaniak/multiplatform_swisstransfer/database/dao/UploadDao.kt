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
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadDao {

    @Query("SELECT * FROM TransferDB WHERE transferStatus=:status ORDER BY createdAt DESC LIMIT 1")
    fun getLastUploadTransferFlow(status: TransferStatus = TransferStatus.PENDING_UPLOAD): Flow<TransferDB>

    @Query("SELECT * FROM TransferDB WHERE transferStatus=:status ORDER BY createdAt DESC")
    suspend fun getAllUploadsTransfers(status: TransferStatus = TransferStatus.PENDING_UPLOAD): List<TransferDB>

    @Query("SELECT * FROM TransferDB WHERE id=:transferId AND transferStatus=:status ORDER BY createdAt DESC")
    suspend fun getTransfer(
        transferId: String,
        status: TransferStatus = TransferStatus.PENDING_UPLOAD,
    ): TransferDB

    @Query("SELECT count(*) FROM transferdb WHERE transferStatus=:status")
    suspend fun getUploadsCount(status: TransferStatus = TransferStatus.PENDING_UPLOAD): Long
}
