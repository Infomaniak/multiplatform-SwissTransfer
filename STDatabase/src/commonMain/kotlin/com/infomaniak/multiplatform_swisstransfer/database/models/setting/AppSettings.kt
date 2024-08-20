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

import io.realm.kotlin.types.RealmObject

class AppSettings : RealmObject {
    private var _theme: String = Theme.SYSTEM.realmKey
    var theme: Theme
        get() = Theme.entries.find { it.realmKey == _theme } ?: DEFAULT_THEME
        set(value) {
            _theme = value.realmKey
        }

    var transferValidityInDays: Int = DEFAULT_TRANSFER_VALIDITY_IN_DAYS
    var limitNumberDownloads: Int = DEFAULT_LIMIT_NUMBER_DOWNLOADS

    private var _language: String = DEFAULT_LANGUAGE.realmKey
    var language: Language
        get() = Language.entries.find { it.realmKey == _language } ?: DEFAULT_LANGUAGE
        set(value) {
            _language = value.realmKey
        }

    companion object {
        private const val DEFAULT_TRANSFER_VALIDITY_IN_DAYS = 30
        private const val DEFAULT_LIMIT_NUMBER_DOWNLOADS = 250

        private val DEFAULT_LANGUAGE = Language.FRENCH //TODO do we want the default language of the phone ?
        private val DEFAULT_THEME = Theme.SYSTEM //TODO do we want the default theme of the phone ?
    }
}

enum class Theme(val realmKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");
}

enum class Language(val realmKey: String) {
    ENGLISH("english"),
    FRENCH("french"),
    GERMAN("german"),
    ITALIAN("italian"),
    SPANISH("spanish");
}
