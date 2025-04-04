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
 * Parent class for attestation tokens exceptions
 * This is thrown whenever an attestation token cannot be used by the API to create a container
 *
 * @param message The message describing the error.
 * @param requestContextId The request context id send by the backend to track the call
 */
sealed class AttestationTokenException(
    message: String,
    requestContextId: String = "",
) : ApiException.ApiErrorException(401, message, requestContextId) {

    /**
     * Thrown when we the attestation token returned by the Api is invalid
     * It could happen either if it's expired or if it has been used to its maximum capacity
     *
     * @param message The message describing the error.
     * @param requestContextId The request context id send by the backend to track the call
     * @property cause The original exception.
     */
    class InvalidAttestationTokenException(
        message: String,
        requestContextId: String = "",
        override val cause: Throwable? = null,
    ) : AttestationTokenException(message, requestContextId)

    /**
     * Thrown when we failed to get a new valid attestation token from the API
     */
    class FailedRetryAttestationTokenException : AttestationTokenException("Failure when getting new attestation token from API")
}
