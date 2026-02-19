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
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.common.utils.DateUtils
import com.infomaniak.multiplatform_swisstransfer.database.controllers.TransferController.Companion.DAYS_SINCE_EXPIRATION
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.FileDB
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.v2.TransferDB
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Dao
interface TransferDao {

    @Query("SELECT * FROM TransferDB WHERE userOwnerId=:userId AND transferStatus!=:uploadStatus ORDER BY createdAt DESC")
    fun getTransfers(
        userId: Long,
        uploadStatus: TransferStatus = TransferStatus.PENDING_UPLOAD,
    ): Flow<List<TransferDB>>

    @OptIn(ExperimentalTime::class)
    @Query(
        """SELECT * FROM TransferDB 
        WHERE userOwnerId=:userId AND transferStatus!=:uploadStatus AND transferDirection=:direction AND expiresAt >= :currentTime
        ORDER BY createdAt DESC """
    )
    fun getValidTransfers(
        userId: Long,
        direction: TransferDirection,
        uploadStatus: TransferStatus = TransferStatus.PENDING_UPLOAD,
        currentTime: Long = Clock.System.now().epochSeconds,
    ): Flow<List<TransferDB>>

    @OptIn(ExperimentalTime::class)
    @Query(
        """SELECT * FROM TransferDB 
        WHERE userOwnerId=:userId AND transferStatus!=:uploadStatus AND transferDirection=:direction AND expiresAt < :currentTime 
        ORDER BY createdAt DESC """
    )
    fun getExpiredTransfers(
        userId: Long,
        direction: TransferDirection,
        uploadStatus: TransferStatus = TransferStatus.PENDING_UPLOAD,
        currentTime: Long = Clock.System.now().epochSeconds,
    ): Flow<List<TransferDB>>

    @Query(
        """SELECT count(*) FROM TransferDB 
        WHERE userOwnerId=:userId AND transferStatus!=:uploadStatus AND transferDirection=:direction"""
    )
    fun getTransfersCount(
        userId: Long,
        direction: TransferDirection,
        uploadStatus: TransferStatus = TransferStatus.PENDING_UPLOAD,
    ): Flow<Int>

    @Query("SELECT * FROM TransferDB WHERE userOwnerId=:userId AND id=:transferId LIMIT 1")
    fun getTransfer(userId: Long, transferId: String): Flow<TransferDB?>

    @Query("SELECT * FROM FileDB WHERE id=:fileId LIMIT 1")
    fun getFile(fileId: String): Flow<FileDB?>

    @Query("SELECT * FROM FileDB WHERE transferId=:transferId AND parentFolderId IS NULL")
    suspend fun getTransferRootFiles(transferId: String): List<FileDB>

    @Query("SELECT * FROM FileDB WHERE transferId=:transferId AND parentFolderId=:folderId")
    fun getTransferFolderFiles(transferId: String, folderId: String?): Flow<List<FileDB>>

    @Query("SELECT * FROM FileDB WHERE parentFolderId=:folderId")
    fun getFilesByFolderId(folderId: String): Flow<List<FileDB>>

    //TODO[API-V2]: suspend fun getNotReadyTransfers(userId: Long): List<TransferDB>

    @Upsert
    suspend fun upsertTransfer(transferDB: TransferDB)

    @Upsert
    suspend fun upsertFile(fileDB: FileDB)

    @Delete
    suspend fun deleteTransfer(transferDB: TransferDB)

    @OptIn(ExperimentalTime::class)
    @Query("DELETE FROM TransferDB WHERE expiresAt < :expiryTime")
    suspend fun deleteExpiredTransfers(
        expiryTime: Long = Clock.System.now().epochSeconds - (DAYS_SINCE_EXPIRATION * DateUtils.SECONDS_IN_A_DAY),
    )

    @Query("DELETE FROM TransferDB WHERE userOwnerId=:userId")
    suspend fun deleteTransfers(userId: Long)
}
