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
package com.infomaniak.multiplatform_swisstransfer.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Online doc](https://developer.infomaniak.com/docs/api/get/1/swisstransfer/users/me)
 */
@Serializable
data class MyUser(
    @SerialName("accounts")
    val organizationAccounts: List<OrganizationAccountApi>,
    @SerialName("default_account_id")
    val defaultOrganizationAccountId: Long,
) {
    @Serializable
    data class OrganizationAccountApi(
        val id: Long,
        val name: String,
        val type: String,
        val pack: String,
        @SerialName("is_in_ksuite")
        val isInKSuite: Boolean,
        val limits: Limits,
    ) {
        @Serializable
        data class Limits(
            @SerialName("transfer_total_size")
            val transferTotalSize: Long,
        )
    }
}
