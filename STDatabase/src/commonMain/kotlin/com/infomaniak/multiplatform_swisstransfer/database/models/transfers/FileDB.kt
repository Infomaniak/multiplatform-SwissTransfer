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
package com.infomaniak.multiplatform_swisstransfer.database.models.transfers

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class FileDB() : File, RealmObject {
    @PrimaryKey
    override var containerUUID: String = ""
    override var uuid: String = ""
    override var fileName: String = ""
    override var fileSizeInBytes: Long = 0L
    override var downloadCounter: Int = 0
    override var createdDateTimestamp: Long = 0L
    override var expiredDateTimestamp: Long = 0L
    override var eVirus: String = ""
    override var deletedDate: String? = null
    override var mimeType: String? = null
    override var receivedSizeInBytes: Long = 0
    override var path: String? = ""
    override var thumbnailPath: String? = ""

    constructor(file: File) : this() {
        this.containerUUID = file.containerUUID
        this.uuid = file.uuid
        this.fileName = file.fileName
        this.fileSizeInBytes = file.fileSizeInBytes
        this.downloadCounter = file.downloadCounter
        this.createdDateTimestamp = file.createdDateTimestamp
        this.expiredDateTimestamp = file.expiredDateTimestamp
        this.eVirus = file.eVirus
        this.deletedDate = file.deletedDate
        this.mimeType = file.mimeType
        this.receivedSizeInBytes = file.receivedSizeInBytes
        this.path = file.path
        this.thumbnailPath = file.thumbnailPath
    }
}
