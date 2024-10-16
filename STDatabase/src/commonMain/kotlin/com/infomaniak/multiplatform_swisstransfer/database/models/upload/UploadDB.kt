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
package com.infomaniak.multiplatform_swisstransfer.database.models.upload

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.Upload
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Class representing files to be uploaded
 */
class UploadDB() : Upload, RealmObject {
    @PrimaryKey
    override var uuid: String = ""
    override var container: UploadContainerDB? = null
    override var uploadHost: String = ""
    override var files = realmListOf<UploadFileDB>()

    init {
        uuid = container?.uuid ?: RealmUUID.random().toString()
    }

    constructor(upload: Upload) : this() {
        this.uuid = upload.uuid
        this.container = upload.container?.let { UploadContainerDB(it) }
        this.uploadHost = upload.uploadHost
        this.files = upload.files.mapTo(realmListOf()) { UploadFileDB(it) }
    }
}
