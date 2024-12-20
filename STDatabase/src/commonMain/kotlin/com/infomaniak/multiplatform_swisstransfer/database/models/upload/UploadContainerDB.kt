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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadContainer
import io.realm.kotlin.types.EmbeddedRealmObject

class UploadContainerDB() : UploadContainer, EmbeddedRealmObject {
    override var uuid: String = ""
    override var duration: String = ""
    override var downloadLimit: Int = 0
    override var language: String = ""
    override var source: String = ""
    override var wsUser: String? = null
    override var authorIP: String = ""
    override var swiftVersion: String = ""
    // var createdDate: String // TODO: Why a complex date instead of a simple date ? May be Custom serial this
    override var expiredDateTimestamp: Long = 0
    override var needPassword: Boolean = false
    override var message: String = ""
    override var numberOfFiles: Int = 0

    constructor(uploadContainer: UploadContainer) : this() {
        this.uuid = uploadContainer.uuid
        this.duration = uploadContainer.duration
        this.downloadLimit = uploadContainer.downloadLimit
        this.language = uploadContainer.language
        this.source = uploadContainer.source
        this.wsUser = uploadContainer.wsUser
        this.authorIP = uploadContainer.authorIP
        this.swiftVersion = uploadContainer.swiftVersion
        this.expiredDateTimestamp = uploadContainer.expiredDateTimestamp
        this.needPassword = uploadContainer.needPassword
        this.message = uploadContainer.message
        this.numberOfFiles = uploadContainer.numberOfFiles
    }
}
