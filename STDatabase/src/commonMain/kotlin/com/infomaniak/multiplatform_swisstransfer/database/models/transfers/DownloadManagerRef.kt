/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer.database.models.transfers

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class DownloadManagerRef private constructor(
    @PrimaryKey
    var id: String,
    var transferUUID: String,
    @Suppress("unused") // We prefer to keep it in the database.
    var fileUid: String?,
    var downloadManagerUniqueId: Long,
) : RealmObject {

    constructor(
        transferUUID: String,
        fileUid: String?,
        downloadManagerUniqueId: Long,
    ) : this(
        id = buildPrimaryKeyForRealm(transferUUID, fileUid),
        transferUUID = transferUUID,
        fileUid = fileUid,
        downloadManagerUniqueId = downloadManagerUniqueId
    )

    @Suppress("unused") // Kept for Realm
    constructor() : this(id = "", transferUUID = "", fileUid = null, downloadManagerUniqueId = 0)

    companion object {
        fun buildPrimaryKeyForRealm(transferUUID: String, fileUid: String?): String {
            return if (fileUid == null) transferUUID else "$transferUUID $fileUid"
        }
    }
}
