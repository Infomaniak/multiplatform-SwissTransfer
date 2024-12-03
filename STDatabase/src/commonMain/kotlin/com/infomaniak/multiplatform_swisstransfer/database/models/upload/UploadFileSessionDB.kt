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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadFileSession
import io.realm.kotlin.types.EmbeddedRealmObject

class UploadFileSessionDB() : UploadFileSession, EmbeddedRealmObject {
    override var name: String = ""
    override var path: String? = null
    override var size: Long = 0L
    override var mimeType: String = ""
    override var localPath: String = ""
    override var remoteUploadFile: RemoteUploadFileDB? = null

    constructor(uploadFileSession: UploadFileSession) : this() {
        this.name = uploadFileSession.name
        this.path = uploadFileSession.path
        this.size = uploadFileSession.size
        this.mimeType = uploadFileSession.mimeType
        this.localPath = uploadFileSession.localPath
    }
}
