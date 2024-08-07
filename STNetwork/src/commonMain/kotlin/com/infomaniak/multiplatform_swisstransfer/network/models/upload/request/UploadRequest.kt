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

import kotlinx.serialization.Serializable

@Serializable
class UploadRequest(
    val duration: String = "",
    val authorEmail: String = "",
    val password: String = "",
    val message: String = "",
    val sizeOfUpload: Long = 0,
    val numberOfDownload: Long = 0,
    val numberOfFile: Long = 0,
    val recaptcha: String = "",
    val recaptchaVersion: Long = 0,
    val lang: String = "",
    val files: List<UploadFileRequest> = emptyList(),
    val recipientsEmails: String = "",
)
