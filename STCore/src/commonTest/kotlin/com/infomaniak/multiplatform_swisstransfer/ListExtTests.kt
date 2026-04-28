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
package com.infomaniak.multiplatform_swisstransfer

import com.infomaniak.multiplatform_swisstransfer.utils.mergeWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ListExtTests {

    @Test
    fun mergeWithBothNonEmpty() {
        val first = listOf(1, 2, 3)
        val second = listOf(4, 5, 6)
        val result = first.mergeWith(second)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), result)
    }

    @Test
    fun mergeWithFirstEmpty() {
        val first = emptyList<Int>()
        val second = listOf(1, 2)
        val result = first.mergeWith(second)
        assertSame(second, result)
    }

    @Test
    fun mergeWithSecondEmpty() {
        val first = listOf(1, 2)
        val second = emptyList<Int>()
        val result = first.mergeWith(second)
        assertSame(first, result)
    }

    @Test
    fun mergeWithBothEmpty() {
        val first = emptyList<Int>()
        val second = emptyList<Int>()
        val result = first.mergeWith(second)
        assertEquals(emptyList(), result)
    }
}
