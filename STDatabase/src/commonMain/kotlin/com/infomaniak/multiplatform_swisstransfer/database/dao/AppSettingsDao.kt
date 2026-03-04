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
import androidx.room.Upsert
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferType
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.v2.AppSettingsDB
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {

    @Query("SELECT * FROM AppSettingsDB LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsDB?>

    @Upsert
    suspend fun put(appSettingsDB: AppSettingsDB)

    @Query("SELECT idOfAccountWithGuestData FROM AppSettingsDB LIMIT 1")
    suspend fun idOfAccountWithGuestData(): Long?

    @Query("UPDATE AppSettingsDB SET idOfAccountWithGuestData = :idOfAccountWithGuestData")
    suspend fun updateIdOfAccountWithGuestData(idOfAccountWithGuestData: Long?)

    @Query("UPDATE AppSettingsDB SET theme = :theme")
    suspend fun updateTheme(theme: Theme)

    @Query("UPDATE AppSettingsDB SET validityPeriod = :validityPeriod")
    suspend fun updateValidityPeriod(validityPeriod: ValidityPeriod)

    @Query("UPDATE AppSettingsDB SET downloadLimit = :downloadLimit")
    suspend fun updateDownloadLimit(downloadLimit: DownloadLimit)

    @Query("UPDATE AppSettingsDB SET emailLanguage = :emailLanguage")
    suspend fun updateEmailLanguage(emailLanguage: EmailLanguage)

    @Query("UPDATE AppSettingsDB SET lastTransferType = :lastTransferType")
    suspend fun updateLastTransferType(lastTransferType: TransferType)

    @Query("UPDATE AppSettingsDB SET lastAuthorEmail = :lastAuthorEmail")
    suspend fun updateLastAuthorEmail(lastAuthorEmail: String?)
}
