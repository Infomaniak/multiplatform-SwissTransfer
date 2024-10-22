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
package com.infomaniak.multiplatform_swisstransfer.database.dataset

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadContainer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadFileSession
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage

object DummyUpload {
    val container = object : UploadContainer {
        override val uuid: String = "uploadContainer1"
        override val duration: String = "30"
        override val downloadLimit: Long = 1
        override val language: String = "Fr-fr"
        override val source: String = "ST"
        override val wsUser: String? = null
        override val authorIP: String = ""
        override val swiftVersion: String = ""
        override val expiredDateTimestamp: Long = 0
        override val needPassword: Boolean = false
        override val message: String = ""
        override val numberOfFiles: Int = 0
    }

    private val upload1 = object : UploadSession {
        override val uuid: String = "upload1"
        override val duration: String = "30"
        override val authorEmail: String = ""
        override val password: String = ""
        override val message: String = ""
        override val numberOfDownload: Int = 0
        override val language: EmailLanguage = EmailLanguage.ITALIAN
        override val recipientsEmails: List<String> = emptyList()
        override val files: List<UploadFileSession> = emptyList()
        override val remoteContainer: UploadContainer? = null
        override val remoteUploadHost: String? = null
    }

    private val upload2 = object : UploadSession by upload1 {
        override val uuid: String = "upload2"
    }

    val uploads = listOf(upload1, upload2)
}
