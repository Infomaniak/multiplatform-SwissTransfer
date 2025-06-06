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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.RemoteUploadFile
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadStatus
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject

class RemoteUploadFileDB() : RemoteUploadFile, EmbeddedRealmObject {

    override var uuid: String = ""
    /**
     * We always try to resume upload, and we check at the end if it went ok.
     * Otherwise, we restart from scratch.
     */
    override var uploadedChunks = realmListOf<Int>()

    private var _uploadStatus: String = UploadStatus.AWAITING.name

    override val uploadStatus: UploadStatus
        get() = enumValueOf<UploadStatus>(_uploadStatus)

    constructor(uuid: String) : this() {
        this.uuid = uuid
    }

    constructor(remoteUploadFile: RemoteUploadFile) : this(remoteUploadFile.uuid) {
        this._uploadStatus = remoteUploadFile.uploadStatus.name
        this.uploadedChunks = remoteUploadFile.uploadedChunks.mapTo(realmListOf()) { it }
    }
}
