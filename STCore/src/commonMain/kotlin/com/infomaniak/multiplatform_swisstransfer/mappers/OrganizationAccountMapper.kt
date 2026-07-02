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
package com.infomaniak.multiplatform_swisstransfer.mappers

import com.infomaniak.multiplatform_swisstransfer.database.models.OrganizationAccount
import com.infomaniak.multiplatform_swisstransfer.network.models.MyUser

internal fun MyUser.OrganizationAccountApi.toDbModel(userId: Long): OrganizationAccount = OrganizationAccount(
    id = id,
    userId = userId,
    name = name,
    type = type,
    pack = pack,
    isInKSuite = isInKSuite,
    limits = limits.toLimits(),
)

internal fun MyUser.OrganizationAccountApi.Limits.toLimits(): OrganizationAccount.Limits = OrganizationAccount.Limits(
    transferTotalSize = transferTotalSize
)
