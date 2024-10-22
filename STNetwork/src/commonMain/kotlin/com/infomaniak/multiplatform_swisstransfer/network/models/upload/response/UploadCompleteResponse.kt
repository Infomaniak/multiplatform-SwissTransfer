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
package com.infomaniak.multiplatform_swisstransfer.network.models.upload.response

import com.infomaniak.multiplatform_swisstransfer.network.serializers.DateToTimestampSerializer
import com.infomaniak.multiplatform_swisstransfer.network.serializers.IntToBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UploadCompleteResponse(
    @SerialName("linkUUID")
    var linkUUID: String = "",
    @SerialName("containerUUID")
    var containerUUID: String = "",
    var userEmail: String? = null,
    var downloadCounterCredit: Long = 0L,
    @SerialName("createdDate")
    @Serializable(DateToTimestampSerializer::class)
    var createdDateTimestamp: Long = 0L,
    @SerialName("expiredDate")
    @Serializable(DateToTimestampSerializer::class)
    var expiredDateTimestamp: Long = 0L,
    @SerialName("isDownloadOnetime")
    @Serializable(with = IntToBooleanSerializer::class)
    var hasBeenDownloadedOneTime: Boolean = false,
    @Serializable(with = IntToBooleanSerializer::class)
    var isMailSent: Boolean = false,
)
