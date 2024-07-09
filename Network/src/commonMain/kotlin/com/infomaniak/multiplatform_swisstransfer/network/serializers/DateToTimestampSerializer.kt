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

package com.infomaniak.multiplatform_swisstransfer.network.serializers

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonPrimitive

internal object DateToTimestampSerializer : JsonTransformingSerializer<Long>(Long.serializer()) {
    @OptIn(FormatStringsInDatetimeFormats::class)
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val dateString = element.jsonPrimitive.content

        val formatter = LocalDateTime.Format {
            byUnicodePattern("uuuu-MM-dd HH:mm:ss")
        }

        val localDateTime = LocalDateTime.parse(dateString, formatter)
        val timestamp = localDateTime.toInstant(TimeZone.UTC).toEpochMilliseconds()

        return runCatching { JsonPrimitive(timestamp) }.getOrDefault(element)
    }
}
