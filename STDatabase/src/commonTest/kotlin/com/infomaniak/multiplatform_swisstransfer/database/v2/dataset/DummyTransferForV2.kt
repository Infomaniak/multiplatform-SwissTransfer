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
package com.infomaniak.multiplatform_swisstransfer.database.v2.dataset

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.File
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus

object DummyTransferForV2 {
    val expired: Transfer
    val notExpired: Transfer

    val transfer1 = object : Transfer {
        override val id: String = "transferLinkUUID1"
        override val senderEmail: String = ""
        override val title: String = ""
        override val message: String = ""
        override val createdAt: Long = 0
        override val expiresAt: Long = 1_730_458_842L // 01/11/2024
        override val totalSize: Long = 0
    }

    val transfer2 = object : Transfer by transfer1 {
        override val id: String = "transfer2"
        override var expiresAt: Long = 4_102_441_200L // 01/01/2100
        override val transferStatus: TransferStatus = TransferStatus.WAIT_VIRUS_CHECK
    }

    val transfer3 = object : Transfer by transfer1 {
        override val id: String = "transfer3"
        override var expiresAt: Long = 4_102_441_200L // 01/01/2100
        override val transferStatus: TransferStatus = TransferStatus.WAIT_VIRUS_CHECK
    }

    val transfer4 = object : Transfer by transfer1 {
        override val id: String = "transfer4"
        override var expiresAt: Long = 4_102_441_200L // 01/01/2100
        override val transferStatus: TransferStatus = TransferStatus.WAIT_VIRUS_CHECK
    }

    init {
        expired = transfer1
        notExpired = transfer2
    }

    val transfers = listOf(transfer1, transfer2, transfer3, transfer4)

    fun createDummyFile(
        path: String,
        mimeType: String? = null,
        id: String = "$path|whatever",
    ): File = object : File {
        override val id: String = id
        override val mimeType: String? = mimeType
        override val path: String = path
        override val size: Long = 10_000_000L
        override val thumbnailPath: String? = null
    }
}
