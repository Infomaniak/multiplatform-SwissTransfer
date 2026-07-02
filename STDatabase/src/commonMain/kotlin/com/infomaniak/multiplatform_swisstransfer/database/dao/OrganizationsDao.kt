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
package com.infomaniak.multiplatform_swisstransfer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.infomaniak.multiplatform_swisstransfer.database.models.OrganizationAccount
import com.infomaniak.multiplatform_swisstransfer.database.models.SelectedOrganizationAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationsDao {

    @Query("SELECT * FROM OrganizationAccount WHERE userId=:userId ORDER BY name")
    suspend fun orgAccountsForUser(userId: Long): List<OrganizationAccount>

    @Query("SELECT organizationAccountId FROM SelectedOrganizationAccount WHERE userId=:userId LIMIT 1")
    fun lastSelectedOrgId(userId: Long): Flow<Long?>

    @Upsert
    suspend fun updateLastSelectedOrganization(orgAccount: SelectedOrganizationAccount)

    @Query("DELETE FROM SelectedOrganizationAccount WHERE userId=:userId")
    suspend fun deleteLastSelectionOrganization(userId: Long)

    @Upsert
    suspend fun updateOrganizations(organizationAccounts: List<OrganizationAccount>)
}
