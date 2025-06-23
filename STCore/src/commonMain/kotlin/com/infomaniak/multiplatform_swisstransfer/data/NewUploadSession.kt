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
package com.infomaniak.multiplatform_swisstransfer.data

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadContainer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadFileSession
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod

data class NewUploadSession(
    override val duration: ValidityPeriod,
    override val authorEmail: String,
    override val authorEmailToken: String? = null,
    override val password: String,
    override val message: String,
    override val numberOfDownload: DownloadLimit,
    override val language: EmailLanguage,
    override val recipientsEmails: Set<String>,
    override val files: List<UploadFileSession>,
    override val uuid: String = "",
    override val remoteContainer: UploadContainer?,
    override val remoteUploadHost: String?,
) : UploadSession {

    constructor(
        duration: ValidityPeriod,
        authorEmail: String,
        authorEmailToken: String?,
        password: String,
        message: String,
        numberOfDownload: DownloadLimit,
        language: EmailLanguage,
        recipientsEmails: Set<String>,
        files: List<UploadFileSession>,
    ) : this(
        duration = duration,
        authorEmail = authorEmail,
        authorEmailToken = authorEmailToken,
        password = password,
        message = message,
        numberOfDownload = numberOfDownload,
        language = language,
        recipientsEmails = recipientsEmails,
        files = files,
        uuid = "",
        remoteContainer = null,
        remoteUploadHost = null
    )
}
