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
package com.infomaniak.multiplatform_swisstransfer.network.exceptions

/**
 * Parent class of API calls exception.
 *
 * This exception is used to represent errors returned by an API, with an associated [requestContextId]
 * and message describing the problem.
 *
 * @param errorMessage The detailed error message explaining the cause of the failure.
 * @param cause The cause of the exception if exists otherwise null
 * @property requestContextId The request context id used to track what happened during calls session by the backend
 */
sealed class ApiException(
    errorMessage: String,
    cause: Throwable?,
    val requestContextId: String,
) : Exception(errorMessage, cause) {

    /**
     * Thrown when an API call fails due to an error identified by a specific error code.
     *
     * This exception is used to represent errors returned by an API, with an associated error code
     * and message describing the problem.
     *
     * @property errorCode The specific error code returned by the API.
     * @property errorMessage The detailed error message explaining the cause of the failure.
     * @param requestContextId The request context id send by the backend to track the call
     */
    open class ApiErrorException(
        val errorCode: Int,
        val errorMessage: String,
        requestContextId: String,
    ) : ApiException(errorMessage, null, requestContextId)

    /**
     * Thrown when an API call returns an error in an unexpected format that cannot be parsed.
     *
     * This exception indicates that the API response format is different from what was expected,
     * preventing proper parsing of the error details.
     *
     * @property statusCode The HTTP status code returned by the API.
     * @property bodyResponse The raw response body from the API that could not be parsed.
     * @param cause The cause of the exception if exists otherwise null
     * @param requestContextId The request context id send by the backend to track the call
     */
    class UnexpectedApiErrorFormatException(
        val statusCode: Int,
        val bodyResponse: String,
        cause: Throwable?,
        requestContextId: String,
    ) : ApiException(bodyResponse, cause, requestContextId)
}
