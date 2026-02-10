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
package com.infomaniak.multiplatform_swisstransfer.managers

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.FileUi
import com.infomaniak.multiplatform_swisstransfer.common.utils.mapToList
import com.infomaniak.multiplatform_swisstransfer.data.STUser
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.controllers.FileController
import com.infomaniak.multiplatform_swisstransfer.mappers.toFileUiList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
    fun getFilesFromTransfer(folderUuid: String): Flow<List<FileUi>> {
        if (accountManager.currentUser is STUser.AuthUser) {
            return appDatabase.getTransferDao().getFilesByFolderId(folderId = folderUuid).map { it.toFileUiList() }
        }
        return fileController.getFilesFromTransfer(folderUuid).map { files -> files.mapToList { FileUi(it) } }
    }
}
