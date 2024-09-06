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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import com.infomaniak.multiplatform_swisstransfer.network.serializers.DateToTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FileApi : File {
    override var containerUUID: String = ""

    @SerialName("UUID")
    override var uuid: String = ""
    override var fileName: String = ""
    override var fileSizeInBytes: Long = 0L
    override var downloadCounter: Long = 0L

    @SerialName("createdDate")
    @Serializable(DateToTimestampSerializer::class)
    override var createdDateTimestamp: Long = 0L

    @SerialName("expiredDate")
    @Serializable(DateToTimestampSerializer::class)
    override var expiredDateTimestamp: Long = 0L
    override var eVirus: String = ""
    override var deletedDate: String? = null
    override var mimeType: String = ""
    override var receivedSizeInBytes: Long = 0L
    override var path: String? = null
}
