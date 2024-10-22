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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadContainer
import com.infomaniak.multiplatform_swisstransfer.network.serializers.DateToTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UploadContainerApi : UploadContainer {
    @SerialName("UUID")
    override var uuid: String = ""
    override var duration: String = ""
    override var downloadLimit: Int = 0
    @SerialName("lang")
    override var language: String = ""
    override var source: String = ""
    @SerialName("WSUser")
    override var wsUser: String? = null
    override var authorIP: String = ""
    override var swiftVersion: String = ""
    // var createdDate: String // TODO: Why a complex date instead of a simple date ? May be Custom serial this
    @SerialName("expiredDate")
    @Serializable(DateToTimestampSerializer::class)
    override var expiredDateTimestamp: Long = 0L
    override var needPassword: Boolean = false
    override var message: String = ""
    @SerialName("numberOfFile")
    override var numberOfFiles: Int = 0
}
