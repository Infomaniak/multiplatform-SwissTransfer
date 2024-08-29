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

package com.infomaniak.multiplatform_swisstransfer.database.models.setting

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.AppSettings
import com.infomaniak.multiplatform_swisstransfer.common.models.DownloadLimit
import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.common.models.Theme
import com.infomaniak.multiplatform_swisstransfer.common.models.ValidityPeriod
import io.realm.kotlin.types.RealmObject

class AppSettingsDB : RealmObject, AppSettings {
    private var _theme: String = Theme.SYSTEM.value
    override var theme: Theme
        get() = Theme.entries.find { it.value == _theme } ?: DEFAULT_THEME
        set(value) {
            _theme = value.value
        }

    private var _validityPeriod: String = ValidityPeriod.THIRTY.value
    override var validityPeriod: ValidityPeriod
        get() = ValidityPeriod.entries.find { it.value == _validityPeriod } ?: DEFAULT_VALIDITY_PERIOD
        set(value) {
            _validityPeriod = value.value
        }

    private var _downloadLimit: String = DownloadLimit.TWOHUNDREDFIFTY.value
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

    companion object {
        private val DEFAULT_THEME = Theme.SYSTEM
        private val DEFAULT_VALIDITY_PERIOD = ValidityPeriod.THIRTY
        private val DEFAULT_DOWNLOAD_LIMIT = DownloadLimit.TWOHUNDREDFIFTY
        private val DEFAULT_EMAIL_LANGUAGE = EmailLanguage.ENGLISH
    }
}
