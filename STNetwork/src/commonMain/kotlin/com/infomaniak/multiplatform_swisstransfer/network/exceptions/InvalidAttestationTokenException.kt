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
package com.infomaniak.multiplatform_swisstransfer.network.exceptions

/**
 * Thrown when we the attestation token returned by the Api is invalid
 * It could happen either if it's expired or if it has been used to its maximum capacity
 *
 * @param message A message explaining what we couldn't find
 * @param requestContextId The request context id send by the backend to track the call
 * @property cause The original exception.
 */
class InvalidAttestationTokenException(message: String, requestContextId: String = "", override val cause: Throwable? = null) :
    ApiException.ApiErrorException(401, message, requestContextId)
