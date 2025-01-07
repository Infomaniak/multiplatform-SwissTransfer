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
package com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers

import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus

interface Transfer {
    val linkUUID: String
    val containerUUID: String
    val downloadCounterCredit: Int
    val createdDateTimestamp: Long
    val expiredDateTimestamp: Long
    val hasBeenDownloadedOneTime: Boolean
    val isMailSent: Boolean
    val downloadHost: String
    val container: Container?

    //region Only local
    val password: String? get() = null
    val transferDirection: TransferDirection? get() = null
    val transferStatus: TransferStatus? get() = null
    val recipients: Set<String> get() = emptySet()
    //endregion
}
