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
package com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.v2

import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus

interface Transfer {
    val id: String
    val senderEmail: String
    val title: String?
    val message: String?
    val createdAt: Long
    val expiresAt: Long
    val files: List<File> get() = emptyList()
    val totalSize: Long

    //region Only local
    val password: String? get() = null
    val transferDirection: TransferDirection get() = TransferDirection.RECEIVED
    val transferStatus: TransferStatus get() = TransferStatus.READY
    val recipientsEmails: Set<String> get() = emptySet()
    //endregion
}
