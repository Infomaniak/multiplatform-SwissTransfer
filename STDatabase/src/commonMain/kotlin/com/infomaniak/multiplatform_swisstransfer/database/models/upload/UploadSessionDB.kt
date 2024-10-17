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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey

class UploadSessionDB() : UploadSession, RealmObject {
    @PrimaryKey
    override var uuid: String = RealmUUID.random().toString()
    override var duration: String = ""
    override var authorEmail: String = ""
    override var password: String = ""
    override var message: String = ""
    override var numberOfDownload: Int = 0
    override var language: String = ""
    override var recipientsEmails: RealmList<String> = realmListOf()
    override var files: RealmList<UploadFileSessionDB> = realmListOf()
    // Remote
    var remoteContainer: UploadContainerDB? = null
    var remoteUploadHost: String? = null

    constructor(uploadSession: UploadSession) : this() {
        this.uuid = uploadSession.uuid
        this.duration = uploadSession.duration
        this.authorEmail = uploadSession.authorEmail
        this.password = uploadSession.password
        this.message = uploadSession.message
        this.numberOfDownload = uploadSession.numberOfDownload
        this.language = uploadSession.language
        this.recipientsEmails = realmListOf(*uploadSession.recipientsEmails.toTypedArray())
        this.files = uploadSession.files.mapTo(realmListOf()) { UploadFileSessionDB(it) }
    }
}
