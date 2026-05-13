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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.BreadcrumbType
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.CrashReportInterface
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.CrashReportLevel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RealmExtTest {

    @Test
    fun catchDbExceptions_ignoresExpectedRealmClosureErrors() = runTest {
        val crashReport = TestCrashReport()

        flow<Int> {
            throw Throwable("Realm closed: RLM_ERR_CLOSED_REALM")
        }
            .catchDbExceptions(crashReport, "Failure to load data from Realm")
            .toList()

        assertTrue(crashReport.capturedErrors.isEmpty())
    }

    @Test
    fun catchDbExceptions_capturesUnexpectedErrors() = runTest {
        val crashReport = TestCrashReport()
        val exception = IllegalStateException("Unexpected exception")

        flow<Int> {
            throw exception
        }
            .catchDbExceptions(crashReport, "Failure to load data from Realm")
            .toList()

        assertEquals(1, crashReport.capturedErrors.size)
        assertEquals("Failure to load data from Realm", crashReport.capturedErrors.first().first)
        assertSame(exception, crashReport.capturedErrors.first().second)
    }

    private class TestCrashReport : CrashReportInterface {
        val capturedErrors = mutableListOf<Pair<String, Throwable>>()

        override fun addBreadcrumb(
            message: String,
            category: String,
            level: CrashReportLevel,
            type: BreadcrumbType,
            data: Map<String, String>?,
        ) = Unit

        override fun capture(
            message: String,
            error: Throwable,
            data: Map<String, String>?,
        ) {
            capturedErrors.add(message to error)
        }

        override fun capture(
            message: String,
            data: Map<String, String>?,
            level: CrashReportLevel?,
        ) = Unit
    }
}
