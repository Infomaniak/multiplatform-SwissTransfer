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
import androidx.room.MapColumn
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
    fun transfersFlow(
        userId: Long,
        uploadStatus: TransferStatus = TransferStatus.PENDING_UPLOAD,
    ): Flow<List<TransferDB>>

    @OptIn(ExperimentalTime::class)
    @Query(
        """SELECT * FROM TransferDB 
        WHERE userOwnerId=:userId AND transferStatus!=:uploadStatus AND transferDirection=:direction AND expiresAt >= :currentTime
        ORDER BY createdAt DESC """
    )
    fun validTransfersFlow(
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
    fun expiredTransfersFlow(
        userId: Long,
        direction: TransferDirection,
        uploadStatus: TransferStatus = TransferStatus.PENDING_UPLOAD,
        currentTime: Long = Clock.System.now().epochSeconds,
    ): Flow<List<TransferDB>>

    @Query(
        """SELECT count(*) FROM TransferDB 
        WHERE userOwnerId=:userId AND transferStatus!=:uploadStatus AND transferDirection=:direction"""
    )
    fun transfersCountFlow(
        userId: Long,
        direction: TransferDirection,
        uploadStatus: TransferStatus = TransferStatus.PENDING_UPLOAD,
    ): Flow<Int>

    @Query("SELECT * FROM TransferDB WHERE userOwnerId=:userId AND id=:transferId LIMIT 1")
    fun transferFlow(userId: Long, transferId: String): Flow<TransferDB?>

    @Query("SELECT * FROM FileDB WHERE id=:fileId LIMIT 1")
    fun fileFlow(fileId: String): Flow<FileDB?>

    @Query("SELECT * FROM TransferDB WHERE id=:transferId LIMIT 1")
    suspend fun getTransfer(transferId: String): TransferDB?

    @Query("SELECT * FROM FileDB WHERE id=:fileId LIMIT 1")
    suspend fun getFile(fileId: String): FileDB?

    @Query("SELECT * FROM FileDB WHERE transferId=:transferId AND NOT isFolder")
    suspend fun getTransferFilesOnly(transferId: String): List<FileDB>

    /**
     * Returns all files located under the given folder path.
     *
     * This uses a lexicographical prefix range instead of `LIKE 'path/%'`:
     *
     *   `path >= folderPath + '/' AND path < folderPath + '/\uFFFF'`
     *
     * Since SQLite compares TEXT values lexicographically, every path starting
     * with `folderPath/` falls within this range. The character `\uFFFF` acts as a
     * maximal Unicode value to cap the upper bound of the prefix.
     *
     * This approach avoids issues with `LIKE` wildcards (`%`, `_`) and allows
     * SQLite to efficiently use an index on `(transferId, path)` for a range scan.
     */
    @Query("SELECT * FROM FileDB WHERE transferId=:transferId AND path >= :folderPath || '/' AND path < :folderPath || '/\uFFFF' AND NOT isFolder")
    suspend fun getFilesUnderPath(transferId: String, folderPath: String): List<FileDB>

    @Query("SELECT * FROM FileDB WHERE transferId=:transferId AND parentId IS NULL")
    suspend fun getTransferRootFiles(transferId: String): List<FileDB>

    @Query("SELECT * FROM FileDB WHERE transferId IN (:transferIds) AND parentId IS NULL")
    suspend fun getTransferRootFiles(transferIds: List<String>): Map<@MapColumn(columnName = "transferId") String, List<FileDB>>

    @Query("SELECT * FROM FileDB WHERE transferId=:transferId AND parentId=:folderId")
    fun transferFolderFilesFlow(transferId: String, folderId: String?): Flow<List<FileDB>>

    @Query("SELECT * FROM FileDB WHERE transferId=:transferId")
    suspend fun getTransferFiles(transferId: String): List<FileDB>

    @Query("SELECT * FROM FileDB WHERE parentId=:folderId")
    fun filesByFolderIdFlow(folderId: String): Flow<List<FileDB>>

    @Query("SELECT * FROM TransferDB WHERE userOwnerId=:userId AND transferStatus='PENDING_UPLOAD' LIMIT 1")
    suspend fun getPendingTransfer(userId: Long): TransferDB?

    @Query("DELETE FROM TransferDB WHERE userOwnerId=:userId AND transferStatus='PENDING_UPLOAD'")
    suspend fun deleteAnyPendingTransfer(userId: Long)

    @Query("UPDATE TransferDB SET transferStatus='READY', linkId=:linkId WHERE id=:transferId")
    suspend fun markPendingTransferAsReady(transferId: String, linkId: String)

    @Query("SELECT * FROM TransferDB WHERE userOwnerId=:userId AND transferStatus NOT IN ('READY', 'PENDING_UPLOAD')")
    suspend fun getUploadedButNotReadyTransfers(userId: Long): List<TransferDB>

    @Upsert
    suspend fun upsertTransfer(transferDB: TransferDB)

    @Query("UPDATE TransferDB SET transferStatus=:transferStatus WHERE id=:transferId")
    suspend fun updateStatus(transferId: String, transferStatus: TransferStatus)

    @Upsert
    suspend fun upsertFile(fileDB: FileDB)

    @Delete
    suspend fun deleteTransfer(transferDB: TransferDB)

    @Query("DELETE FROM TransferDB WHERE id=:transferId")
    suspend fun deleteTransfer(transferId: String)

    @OptIn(ExperimentalTime::class)
    @Query("DELETE FROM TransferDB WHERE expiresAt < :expiryTime")
    suspend fun deleteExpiredTransfers(
        expiryTime: Long = Clock.System.now().epochSeconds - (DAYS_SINCE_EXPIRATION * DateUtils.SECONDS_IN_A_DAY),
    )

    @Query("DELETE FROM TransferDB WHERE userOwnerId=:userId")
    suspend fun deleteTransfers(userId: Long)
}
