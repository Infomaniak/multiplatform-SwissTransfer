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
package com.infomaniak.multiplatform_swisstransfer.utils

/**
 * Merges this list with the given list, returning a new list containing all elements of both.
 *
 * @param otherList The list to merge with this list.
 * @return A new list with all elements of this list followed by all elements of the other list.
 */
internal fun <T> List<T>.mergeWith(otherList: List<T>): List<T> {
    if (this.isEmpty()) return otherList
    if (otherList.isEmpty()) return this

    return ArrayList<T>(this.size + otherList.size).apply {
        addAll(this)
        addAll(otherList)
    }
}
