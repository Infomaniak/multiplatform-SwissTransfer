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
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.infomaniak.multiplatform_swisstransfer.database.models.EmailValidation

@Dao
interface EmailValidationsDao {

    @Upsert
    suspend fun upsertEmailValidation(validations: EmailValidation)

    @Query("SELECT * FROM EmailValidation WHERE email=:email")
    suspend fun getEmailValidation(email: String): EmailValidation?

    @Query("UPDATE EmailValidation SET validationRequiredByTimestampSeconds = NULL WHERE id=:validationId")
    suspend fun markAsValidated(validationId: String)

    @Query("UPDATE EmailValidation " +
            "SET validationRequiredByTimestampSeconds =:validationRequiredByTimestampSeconds " +
            "WHERE id=:validationId")
    suspend fun updatePendingValidation(validationId: String, validationRequiredByTimestampSeconds: Long)
}
