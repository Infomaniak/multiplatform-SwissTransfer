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
package com.infomaniak.multiplatform_swisstransfer.network.models.transfer

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Container
import com.infomaniak.multiplatform_swisstransfer.network.serializers.DateToTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ContainerApi : Container<List<FileApi>> {
    @SerialName("UUID")
    override var uuid: String = ""

    override var duration: Long = 0L

    @SerialName("createdDate")
    @Serializable(DateToTimestampSerializer::class)
    override var createdDateTimestamp: Long = 0L

    @SerialName("expiredDate")
    @Serializable(DateToTimestampSerializer::class)
    override var expiredDateTimestamp: Long = 0L
    override var numberOfFile: Long = 0L
    override var message: String? = null
    override var needPassword: Long = 0L
    override var lang: String = ""
    override var sizeUploaded: Long = 0L

    @SerialName("deletedDate")
    @Serializable(DateToTimestampSerializer::class)
    override var deletedDateTimestamp: Long? = null
    override var swiftVersion: Long = 0L
    override var downloadLimit: Long = 0L
    override var source: String = "ST"

//    @SerialName("WSUser") TODO: What's it ?
//    var wsUser: JsonElement? = null

    override var files: List<FileApi> = emptyList()
}
