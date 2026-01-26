/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer.network.models.transfer.v2

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.Transfer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferApi(
    override val id: String,
    @SerialName("sender_email")
    override val senderEmail: String,
    override val title: String? = null,
    override val message: String? = null,
    @SerialName("created_at")
    override val createdAt: Long,
    @SerialName("expires_at")
    override val expiresAt: Long,
    override val files: List<FileApi>,
    @SerialName("total_size")
    override val totalSize: Long,
) : Transfer
