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
package com.infomaniak.multiplatform_swisstransfer.database

/**
 * Example:
 *
 * For the input `"1/2/3/4/whatever"` and a [delimiter] set to `'/'`, [action] will be called with:
 * - `"1"`
 * - `"1/2"`
 * - `"1/2/3"`
 * - `"1/2/3/4"`
 * - `"1/2/3/4/whatever"`
 */
internal inline fun String.forEachSubstring(
    delimiter: Char,
    action: (substring: String) -> Unit
) {
    var lastOccurrenceIndex = 0
    do {
        lastOccurrenceIndex = indexOf(delimiter, startIndex = lastOccurrenceIndex)
        val found = lastOccurrenceIndex != -1
        action(if (found) substring(0, lastOccurrenceIndex) else this)
        lastOccurrenceIndex += 1 // Delimiter length, to skip the previous occurrence.
    } while (found)
}
