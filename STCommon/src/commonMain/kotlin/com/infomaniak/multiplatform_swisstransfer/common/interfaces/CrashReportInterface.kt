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

enum class CrashReportLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    FATAL
}

/**
 * Breadcrumb types for Sentry. Controls the icon/color in the UI.
 *
 * @param value The value used in the JSON payload.
 * @see <a href="https://develop.sentry.dev/sdk/data-model/event-payloads/breadcrumbs/#breadcrumb-types">Sentry Documentation</a>
 */
enum class BreadcrumbType(val value: String) {
    Default("default"),
    HTTP("http"),
}

interface CrashReportInterface {
    /**
     * Adds a breadcrumb to the crash reporting system to provide contextual information
     * leading up to a potential crash.
     *
     * @param message A descriptive message for the breadcrumb, explaining the event or action.
     * @param category A category string to group related breadcrumbs (e.g., "UI", "Network").
     * @param level The severity level of the breadcrumb (e.g., info, warning, error).
     * @param type Sentry internal attribute that controls how breadcrumbs are categorized.
     * @param data Optional additional data providing more context about the event.
     */
    fun addBreadcrumb(
        message: String,
        category: String,
        level: CrashReportLevel,
        type: BreadcrumbType = BreadcrumbType.Default,
        data: Map<String, String>? = null,
    )

    /**
     * Captures and reports an error to the crash reporting system with optional context
     * and additional metadata.
     *
     * @param message A custom message to be reported (e.g., an error message or event description).
     * @param error The [Throwable] to be reported.
     * @param data Optional contextual data to provide more insight into the environment or state when the error occurred.
     */
    fun capture(
        message: String,
        error: Throwable,
        data: Map<String, String>? = null,
    )

    /**
     * Captures a custom message and reports it to the crash reporting system with optional context,
     * severity level, and additional metadata.
     *
     * @param message The custom message to be reported (e.g., an error message or event description).
     * @param data Optional contextual data that provides additional information about the environment
     *                or state when the message was logged.
     * @param level The severity level of the message (e.g., `info`, `warning`, `error`).
     */
    fun capture(
        message: String,
        data: Map<String, String>? = null,
        level: CrashReportLevel? = null
    )
}
