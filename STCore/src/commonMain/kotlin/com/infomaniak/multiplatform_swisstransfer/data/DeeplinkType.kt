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
package com.infomaniak.multiplatform_swisstransfer.data

open class DeeplinkType(uuid: String) {
    data class DeleteTransfer(val uuid: String, val token: String): DeeplinkType(uuid) {
        companion object {
            private val REGEX = "^https://.+/d/(?<uuid>[^?]+)(\\?delete=(?<token>.+))?$".toRegex()

            fun fromURL(url: String): DeleteTransfer? {
                var matchResult = REGEX.matchEntire(url) ?: return null

                val uuid = matchResult.groups["uuid"]?.value ?: return null
                val token = matchResult.groups["token"]?.value ?: return null
                return DeleteTransfer(uuid, token)
            }
        }
    }

    data class OpenTransfer(val uuid: String): DeeplinkType(uuid) {
        companion object {
            private val REGEX = "^https://.+/d/(?<uuid>[^?]+)$".toRegex()

            fun fromURL(url: String): OpenTransfer? {
                var matchResult = REGEX.matchEntire(url) ?: return null

                val uuid = matchResult.groups["uuid"]?.value ?: return null
                return OpenTransfer(uuid)
            }
        }
    }

    companion object {
        fun fromURL(url: String): DeeplinkType? {
            return DeleteTransfer.fromURL(url) ?: OpenTransfer.fromURL(url)
        }
    }
}
