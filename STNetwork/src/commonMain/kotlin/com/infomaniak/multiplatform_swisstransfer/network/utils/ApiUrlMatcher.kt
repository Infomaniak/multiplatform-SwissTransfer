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
package com.infomaniak.multiplatform_swisstransfer.network.utils

object ApiUrlMatcher {
    private const val v1UrlRegex = "^https://.+/d/[^?]+"
    private const val v2UrlRegex = "^https://.+/dl/[^?]+"

    fun isV1Url(url: String) = url.contains("/d/") && v1UrlRegex.toRegex().matches(url)
    fun isV2Url(url: String) = url.contains("/dl/") && v2UrlRegex.toRegex().matches(url)

    fun extractUUID(url: String) = url.substringAfterLast("/")
}
