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

import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.AttestationTokenException.InvalidAttestationTokenException

/**
 * A sealed class representing various container error exceptions that extend [ApiErrorException].
 * This class is used to handle API errors that occur when creating a container through the API.
 * Each specific error exception is associated with an HTTP status code and a descriptive error message.
 *
 * @constructor Creates a [ContainerErrorsException] with the given status code and error message.
 *
 * @property statusCode The HTTP status code for the error.
 * @param errorMessage A descriptive error message for the error.
 * @param requestContextId The request context id send by the backend to track the call
 */
sealed class ContainerErrorsException(
    val statusCode: Int,
    errorMessage: String,
    requestContextId: String,
) : ApiErrorException(statusCode, errorMessage, requestContextId) {

    /**
     * Exception indicating that email address validation is required.
     * This corresponds to an HTTP 401 Unauthorized status.
     *
     * @param requestContextId The request context id send by the backend to track the call
     */
    class EmailValidationRequired(requestContextId: String) :
        ContainerErrorsException(401, "Email address validation required", requestContextId)

    /**
     * Exception indicating that the domain was automatically blocked for security reasons.
     * This corresponds to an HTTP 403 Forbidden status.
     *
     * @param requestContextId The request context id send by the backend to track the call
     */
    class DomainBlockedException(requestContextId: String) :
        ContainerErrorsException(403, "The domain was automatically blocked for security reasons", requestContextId)

    /**
     * Exception indicating that the daily transfer limit has been reached.
     * This corresponds to an HTTP 404 Not Found status.
     *
     * @param requestContextId The request context id send by the backend to track the call
     */
    class DailyTransferLimitReachedException(requestContextId: String) :
        ContainerErrorsException(404, "Daily transfer limit reached", requestContextId)

    /**
     * Exception indicating that the provided captcha is not valid.
     * This corresponds to an HTTP 422 Unprocessable Entity status.
     *
     * @param requestContextId The request context id send by the backend to track the call
     */
    class CaptchaNotValidException(requestContextId: String) :
        ContainerErrorsException(422, "Captcha not valid", requestContextId)

    /**
     * Exception indicating that too many codes have been generated.
     * This corresponds to an HTTP 429 Too Many Requests status.
     *
     * @param requestContextId The request context id send by the backend to track the call
     */
    class TooManyCodesGeneratedException(requestContextId: String) :
        ContainerErrorsException(429, "Too many codes generated", requestContextId)

    /**
     * Exception indicating that the daily quota have been exceeded.
     * This corresponds to an HTTP 429 Too Many Requests status.
     *
     * @param requestContextId The request context id send by the backend to track the call
     */
    class DailyQuotaExceededException(requestContextId: String) :
        ContainerErrorsException(429, "Daily transfer limit reached", requestContextId)

    internal companion object {

        private const val EXPIRED_TOKEN_CODE = "expired_token"
        private const val JWT_LIMIT_REACHED_CODE = "jwt_limit_reached"
        private const val INVALID_JWT_CODE = "invalid_jwt"
        private const val DAILY_TRANSFER_LIMIT_CODE = "Daily transfer limit reached"
        private const val EMAIL_VALIDATION_REQUIRED_CODE = "Email address validation required"

        /**
         * Extension function to convert an instance of [ApiException.UnexpectedApiErrorFormatException] to a more specific exception
         * based on its HTTP status code.
         *
         * This function maps the status codes to specific exceptions as follows:
         * - 401: [EmailValidationRequired]
         * - 403: [DomainBlockedException]
         * - 404: [DailyTransferLimitReachedException]
         * - 422: [CaptchaNotValidException]
         * - 429: [TooManyCodesGeneratedException]
         * - Other status codes: The original [ApiException.UnexpectedApiErrorFormatException] instance
         *
         * @receiver An instance of [ApiException.UnexpectedApiErrorFormatException].
         * @return An instance of [Exception] which can be one of the specific exceptions mentioned above,
         * or the original [ApiException.UnexpectedApiErrorFormatException] if the status code does not match any predefined values.
         */
        fun UnexpectedApiErrorFormatException.toContainerErrorsException(): Exception {

            fun UnexpectedApiErrorFormatException.manage401Errors() = when {
                message?.contains(EXPIRED_TOKEN_CODE) == true -> {
                    InvalidAttestationTokenException(EXPIRED_TOKEN_CODE, requestContextId)
                }
                message?.contains(JWT_LIMIT_REACHED_CODE) == true -> {
                    InvalidAttestationTokenException(JWT_LIMIT_REACHED_CODE, requestContextId)
                }
                message?.contains(INVALID_JWT_CODE) == true -> {
                    InvalidAttestationTokenException(INVALID_JWT_CODE, requestContextId)
                }
                message?.contains(EMAIL_VALIDATION_REQUIRED_CODE) == true -> {
                    EmailValidationRequired(requestContextId)
                }
                else -> this
            }

            fun UnexpectedApiErrorFormatException.manage429Errors() = when {
                message?.contains(DAILY_TRANSFER_LIMIT_CODE) == true -> {
                    DailyQuotaExceededException(requestContextId)
                }
                else -> {
                    TooManyCodesGeneratedException(requestContextId)
                }
            }

            return when (statusCode) {
                401 -> manage401Errors()
                403 -> DomainBlockedException(requestContextId)
                404 -> DailyTransferLimitReachedException(requestContextId)
                422 -> CaptchaNotValidException(requestContextId)
                429 -> manage429Errors()
                else -> this
            }
        }
    }
}
