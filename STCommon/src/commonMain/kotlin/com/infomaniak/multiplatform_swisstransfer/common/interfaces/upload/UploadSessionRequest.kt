/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload

import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod

class UploadSessionRequest(
    val validityPeriod: ValidityPeriod,
    val authorEmail: String,
    val password: String,
    val message: String,
    val sizeOfUpload: Long,
    val downloadCountLimit: DownloadLimit,
    val filesCount: Int,
    val languageCode: EmailLanguage,
    val filesMetadata: List<FileToUploadMetadata>,
    val recipientsEmails: Set<String>,
)
