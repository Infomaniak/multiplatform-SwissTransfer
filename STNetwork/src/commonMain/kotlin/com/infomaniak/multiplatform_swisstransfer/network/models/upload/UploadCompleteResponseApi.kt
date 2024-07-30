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

package com.infomaniak.multiplatform_swisstransfer.network.models.upload

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadCompleteResponse
import com.infomaniak.multiplatform_swisstransfer.network.serializers.DateToTimestampSerializer
import com.infomaniak.multiplatform_swisstransfer.network.serializers.IntToBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UploadCompleteResponseApi : UploadCompleteResponse {
    override var linkUUID: String = ""
    override var containerUUID: String = ""
    override var userEmail: String? = null
    override var downloadCounterCredit: Long = 0

    @SerialName("createdDate")
    @Serializable(DateToTimestampSerializer::class)
    override var createdDateTimestamp: Long = 0

    @SerialName("expiredDate")
    @Serializable(DateToTimestampSerializer::class)
    override var expiredDateTimestamp: Long = 0

    @Serializable(with = IntToBooleanSerializer::class)
    override var isDownloadOnetime: Boolean = false

    @Serializable(with = IntToBooleanSerializer::class)
    override var isMailSent: Boolean = false
}
