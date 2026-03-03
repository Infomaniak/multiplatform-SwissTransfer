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
package com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.v2

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferType
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod

@Entity
data class AppSettingsDB(
    @PrimaryKey
    private val id: String = "Default",
    override val theme: Theme = Theme.SYSTEM,
    override val validityPeriod: ValidityPeriod = ValidityPeriod.THIRTY,
    override val downloadLimit: DownloadLimit = DownloadLimit.TWO_HUNDRED_FIFTY,
    override val emailLanguage: EmailLanguage = EmailLanguage.ENGLISH,
    override val lastTransferType: TransferType = TransferType.LINK,
    override val lastAuthorEmail: String? = null,
    val dataMigrated: Boolean = false,
    //TODO[ST-v2]: id of account with guest data
) : AppSettings {

    constructor() : this(theme = Theme.SYSTEM)

    constructor(emailLanguage: EmailLanguage) : this(
        theme = Theme.SYSTEM,
        emailLanguage = emailLanguage,
    )

    constructor(appSettings: AppSettings) : this(
        theme = appSettings.theme,
        validityPeriod = appSettings.validityPeriod,
        downloadLimit = appSettings.downloadLimit,
        emailLanguage = appSettings.emailLanguage,
        lastTransferType = appSettings.lastTransferType,
        lastAuthorEmail = appSettings.lastAuthorEmail,
    )
}
