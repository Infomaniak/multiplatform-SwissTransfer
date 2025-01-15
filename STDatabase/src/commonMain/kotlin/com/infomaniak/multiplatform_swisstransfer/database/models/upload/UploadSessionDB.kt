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
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.AppSettingsDB
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey

class UploadSessionDB() : UploadSession, RealmObject {
    @PrimaryKey
    override var uuid: String = RealmUUID.random().toString()
    private var _duration: Int = AppSettingsDB.DEFAULT_VALIDITY_PERIOD.value
    override var authorEmail: String = ""
    override var authorEmailToken: String? = null
    override var password: String = ""
    override var message: String = ""
    private var _numberOfDownload: Int = AppSettingsDB.DEFAULT_DOWNLOAD_LIMIT.value
    override var recipientsEmails: RealmList<String> = realmListOf()
    override var files: RealmList<UploadFileSessionDB> = realmListOf()
    private var _language: String = ""

    // Remote
    override var remoteContainer: UploadContainerDB? = null
    override var remoteUploadHost: String? = null

    override val duration: ValidityPeriod get() = ValidityPeriod.entries.first { it.value == _duration }
    override val numberOfDownload: DownloadLimit get() = DownloadLimit.entries.first { it.value == _numberOfDownload }
    override var language: EmailLanguage
        get() = EmailLanguage.entries.first { it.code == _language }
        set(value) {
            _language = value.code
        }

    constructor(uploadSession: UploadSession) : this() {
        if (uploadSession.uuid.isNotEmpty()) this.uuid = uploadSession.uuid
        this._duration = uploadSession.duration.value
        this.authorEmail = uploadSession.authorEmail
        this.authorEmailToken = uploadSession.authorEmailToken
        this.password = uploadSession.password
        this.message = uploadSession.message
        this._numberOfDownload = uploadSession.numberOfDownload.value
        this.language = uploadSession.language
        this.recipientsEmails = uploadSession.recipientsEmails.mapTo(destination = realmListOf(), transform = { it })
        this.files = uploadSession.files.mapTo(realmListOf(), ::UploadFileSessionDB)
    }
}
