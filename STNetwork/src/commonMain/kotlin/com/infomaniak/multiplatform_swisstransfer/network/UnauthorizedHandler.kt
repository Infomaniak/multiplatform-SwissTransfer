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
package com.infomaniak.multiplatform_swisstransfer.network

/**
 * Handles unauthorized access events (HTTP 401).
 *
 * This interface is intended to be implemented by the application layer in order to react
 * to authentication failures detected at the network level.
 *
 * Typical use cases include:
 * - Clearing user session data
 * - Redirecting to a login screen
 * - Triggering a global logout
 */
fun interface UnauthorizedHandler {

    /**
     * Called when an unauthorized response (HTTP 401) is detected.
     *
     * This usually indicates that the current authentication state is no longer valid
     * (e.g. expired or invalid token).
     */
    suspend fun onUnauthorized(userId: Long?)
}
