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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.infomaniak.multiplatform_swisstransfer.managers

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.FileUi
import com.infomaniak.multiplatform_swisstransfer.common.utils.mapToList
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.controllers.FileController
import com.infomaniak.multiplatform_swisstransfer.mappers.toFileUi
import com.infomaniak.multiplatform_swisstransfer.mappers.toFileUiList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class FileManager(
    private val accountManager: AccountManager,
    private val appDatabase: AppDatabase,
    private val fileController: FileController,
) {

    /**
     * Retrieves a flow of files contained in a folder with the specified folderUuid.
     *
     * @param folderUuid The UUID of the folder within the transfer.
     * @return A flow of lists of [FileUi] objects representing the files in the transfer.
     */
    fun getFilesFromTransfer(
        folderUuid: String
    ): Flow<List<FileUi>> = accountManager.currentUserFlow.transformLatest { currentUser ->
        when (currentUser) {
            is STUser.AuthUser -> {
                emitAll(appDatabase.getTransferDao().filesByFolderIdFlow(folderId = folderUuid).map { it.toFileUiList() })
            }
            else -> emit(emptyList())
        }
    }.transformLatest { files ->
        if (files.isEmpty()) {
            emitAll(fileController.getFilesFromTransfer(folderUuid).map { files -> files.mapToList { FileUi(it) } })
        } else {
            emit(files)
        }
    }

    /**
     * Retrieves a single file by its unique identifier.
     *
     * @param fileId The unique identifier of the file.
     * @return A [FileUi] object representing the file, or null if not found.
     */
    suspend fun getFileUi(fileId: String): FileUi? {
        return appDatabase.getTransferDao().getFile(fileId)?.toFileUi()
    }

    /**
     * Retrieves all files (not folders) for a given transfer.
     *
     * @param transferId The ID of the transfer.
     * @return A list of [FileUi] objects representing all files in the transfer.
     */
    @Throws(CancellationException::class)
    suspend fun getTransferFilesOnly(transferId: String): List<FileUi> = withContext(Dispatchers.Default) {
        appDatabase.getTransferDao().getTransferFilesOnly(transferId).toFileUiList()
    }

    /**
     * Retrieves all files under a specific folder path, including subfolders (not folders).
     *
     * @param transferId The ID of the transfer.
     * @param folderPath The path of the folder to search under (e.g., "documents").
     * @return A list of [FileUi] objects representing files in the folder and its subfolders.
     */
    @Throws(CancellationException::class)
    suspend fun getFilesUnderPath(transferId: String, folderPath: String): List<FileUi> = withContext(Dispatchers.Default) {
        appDatabase.getTransferDao().getFilesUnderPath(transferId, folderPath).toFileUiList()
    }
}
