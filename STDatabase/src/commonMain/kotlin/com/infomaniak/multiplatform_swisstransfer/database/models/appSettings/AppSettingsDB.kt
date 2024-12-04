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
package com.infomaniak.multiplatform_swisstransfer.database.models.appSettings

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.*
import io.realm.kotlin.types.RealmObject

class AppSettingsDB() : RealmObject, AppSettings {

    constructor(emailLanguage: EmailLanguage) : this() {
        this.emailLanguage = emailLanguage
    }

    //region Options available in App Settings
    private var _theme: String = Theme.SYSTEM.value
    override var theme: Theme
        get() = Theme.entries.find { it.value == _theme } ?: DEFAULT_THEME
        set(value) {
            _theme = value.value
        }

    private var _validityPeriod: Int = ValidityPeriod.THIRTY.value
    override var validityPeriod: ValidityPeriod
        get() = ValidityPeriod.entries.find { it.value == _validityPeriod } ?: DEFAULT_VALIDITY_PERIOD
        set(value) {
            _validityPeriod = value.value
        }

    private var _downloadLimit: Int = DownloadLimit.TWO_HUNDRED_FIFTY.value
    override var downloadLimit: DownloadLimit
        get() = DownloadLimit.entries.find { it.value == _downloadLimit } ?: DEFAULT_DOWNLOAD_LIMIT
        set(value) {
            _downloadLimit = value.value
        }

    private var _emailLanguage: String = DEFAULT_EMAIL_LANGUAGE.value
    override var emailLanguage: EmailLanguage
        get() = EmailLanguage.entries.find { it.value == _emailLanguage } ?: DEFAULT_EMAIL_LANGUAGE
        set(value) {
            _emailLanguage = value.value
        }
    //endregion

    private var _lastTransferType: String = DEFAULT_TRANSFER_TYPE.name
    override var lastTransferType: TransferType
        get() = TransferType.entries.find { it.name == _lastTransferType } ?: DEFAULT_TRANSFER_TYPE
        set(value) {
            _lastTransferType = value.name
        }

    override var lastAuthorEmail: String? = null

    companion object {
        val DEFAULT_VALIDITY_PERIOD = ValidityPeriod.THIRTY
        val DEFAULT_DOWNLOAD_LIMIT = DownloadLimit.TWO_HUNDRED_FIFTY

        private val DEFAULT_EMAIL_LANGUAGE = EmailLanguage.ENGLISH
        private val DEFAULT_THEME = Theme.SYSTEM
        private val DEFAULT_TRANSFER_TYPE = TransferType.QR_CODE
    }
}
