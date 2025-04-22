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
package com.infomaniak.multiplatform_swisstransfer.common.interfaces

public enum class CrashReportLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    FATAL
}

public interface CrashReportInterface {

    /**
     * Adds a breadcrumb to the crash reporting system to provide contextual information
     * leading up to a potential crash.
     *
     * @param message A descriptive message for the breadcrumb, explaining the event or action.
     * @param category A category string to group related breadcrumbs (e.g., "UI", "Network").
     * @param level The severity level of the breadcrumb (e.g., info, warning, error).
     * @param metadata Optional additional metadata providing more context about the event.
     */
    fun addBreadcrumb(
        message: String,
        category: String,
        level: CrashReportLevel,
        metadata: Map<String, Any>? = null
    )

    /**
     * Captures and reports an error to the crash reporting system with optional context
     * and additional metadata.
     *
     * @param error The [Throwable] to be reported.
     * @param context Optional contextual data to provide more insight into the environment
     *                or state when the error occurred.
     * @param contextKey An optional key to identify or categorize the provided context.
     */
    fun capture(
        error: Throwable,
        context: Map<String, Any>? = null,
        contextKey: String? = null
    )

    /**
     * Captures a custom message and reports it to the crash reporting system with optional context,
     * severity level, and additional metadata.
     *
     * @param message The custom message to be reported (e.g., an error message or event description).
     * @param context Optional contextual data that provides additional information about the environment
     *                or state when the message was logged.
     * @param contextKey An optional key to categorize or identify the provided context data.
     * @param level The severity level of the message (e.g., `info`, `warning`, `error`).
     */
    fun capture(
        message: String,
        context: Map<String, Any>? = null,
        contextKey: String? = null,
        level: CrashReportLevel? = null
    )
}

