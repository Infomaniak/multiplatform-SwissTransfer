/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer.utils

import com.infomaniak.multiplatform_swisstransfer.data.DeepLinkType

open class DeepLinkTypeIos(uuid: String) : DeepLinkType(uuid) {
    data class ImportTransferFromExtension(val uuid: String): DeepLinkTypeIos(uuid) {
        companion object {
            private val REGEX = "^https://.+/import\\?uuid=(?<uuid>[^?]+)$".toRegex()

            fun fromURL(url: String): ImportTransferFromExtension? {
                val matchResult = REGEX.matchEntire(url) ?: return null

                val uuid = matchResult.groups["uuid"]?.value ?: return null
                return ImportTransferFromExtension(uuid)
            }
        }
    }

    companion object {
        fun fromURL(url: String): DeepLinkType? {
            return DeepLinkType.fromURL(url) ?: ImportTransferFromExtension.fromURL(url)
        }
    }
}
