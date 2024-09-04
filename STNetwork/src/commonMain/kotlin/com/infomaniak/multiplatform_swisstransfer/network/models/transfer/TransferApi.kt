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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.Transfer
import com.infomaniak.multiplatform_swisstransfer.network.serializers.DateToTimestampSerializer
import com.infomaniak.multiplatform_swisstransfer.network.serializers.IntToBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TransferApi : Transfer<ContainerApi> {
    override var linkUUID: String = ""
    override var containerUUID: String = ""
    override var downloadCounterCredit: Long = 0

    @SerialName("createdDate")
    @Serializable(DateToTimestampSerializer::class)
    override var createdDateTimestamp: Long = 0

    @SerialName("expiredDate")
    @Serializable(DateToTimestampSerializer::class)
    override var expiredDateTimestamp: Long = 0
    override var isDownloadOnetime: Long = 0

    @Serializable(with = IntToBooleanSerializer::class)
    override var isMailSent: Boolean = false
    override var downloadHost: String = ""
    override var container: ContainerApi = ContainerApi()

    // TODO: Add download method url
    val downloadUrl get() = "https://$downloadHost/api/download/??Quoi "
    // https://dl-j5769qrh.swisstransfer.com/api/download/ec6bc7ac-96b3-4a6e-8d30-8e12e379d11a/97a742c9-b0f5-4fa5-b6bf-cb2c2d6bbe94
}
