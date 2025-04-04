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
package com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import com.infomaniak.multiplatform_swisstransfer.common.utils.mapToList

data class TransferUi(
    val uuid: String,
    val createdDateTimestamp: Long,
    val expirationDateTimestamp: Long,
    val sizeUploaded: Long,
    val downloadLimit: Int,
    val downloadLeft: Int,
    val message: String?,
    val password: String?,
    val recipientsEmails: Set<String> = emptySet(),
    val files: List<FileUi>,
    val direction: TransferDirection? = null,
    val transferStatus: TransferStatus? = null
) {
    constructor(transfer: Transfer) : this(
        uuid = transfer.linkUUID,
        createdDateTimestamp = transfer.createdDateTimestamp,
        expirationDateTimestamp = transfer.expiredDateTimestamp,
        sizeUploaded = transfer.container?.sizeUploaded ?: 0,
        downloadLimit = transfer.container?.downloadLimit ?: 0,
        downloadLeft = transfer.downloadCounterCredit,
        message = transfer.container?.message,
        password = transfer.password,
        recipientsEmails = transfer.recipientsEmails.mapTo(destination = mutableSetOf(), transform = { it }),
        files = transfer.container?.files?.mapToList(::FileUi) ?: emptyList(),
        direction = transfer.transferDirection,
        transferStatus = transfer.transferStatus,
    )
}
