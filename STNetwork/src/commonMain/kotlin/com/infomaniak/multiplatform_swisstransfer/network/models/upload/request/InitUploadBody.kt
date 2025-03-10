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
package com.infomaniak.multiplatform_swisstransfer.network.models.upload.request

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSessionRequest
import com.infomaniak.multiplatform_swisstransfer.common.utils.mapToList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class InitUploadBody(
    val duration: String = "",
    val authorEmail: String = "",
    val authorEmailToken: String? = null,
    val password: String = "",
    val message: String = "",
    val sizeOfUpload: Long = 0L,
    val numberOfDownload: Int = 0,
    val numberOfFile: Int = 0,
    @SerialName("lang")
    val language: String = "",
    val files: String = "", // List<UploadFileRequest>
    val recipientsEmails: String = "",
) {
    constructor(uploadSession: UploadSession) : this(
        duration = uploadSession.duration.value.toString(),
        authorEmail = uploadSession.authorEmail,
        authorEmailToken = uploadSession.authorEmailToken,
        password = uploadSession.password,
        message = uploadSession.message,
        sizeOfUpload = uploadSession.files.sumOf { it.size },
        numberOfDownload = uploadSession.numberOfDownload.value,
        numberOfFile = uploadSession.files.count(),
        language = uploadSession.language.code,
        files = Json.encodeToString(uploadSession.files.mapToList(::UploadFileRequest)),
        recipientsEmails = Json.encodeToString(uploadSession.recipientsEmails),
    )

    constructor(req: UploadSessionRequest, authorEmailToken: String?) : this(
        duration = req.validityPeriod.value.toString(),
        authorEmail = req.authorEmail,
        authorEmailToken = authorEmailToken,
        password = req.password,
        message = req.message,
        sizeOfUpload = req.sizeOfUpload,
        numberOfDownload = req.downloadCountLimit.value,
        numberOfFile = req.filesCount,
        language = req.languageCode.code,
        files = Json.encodeToString(req.filesMetadata.map(::UploadFileRequest)),
        recipientsEmails = Json.encodeToString(req.recipientsEmails),
    )
}
