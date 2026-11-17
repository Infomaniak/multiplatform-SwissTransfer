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
package com.infomaniak.multiplatform_swisstransfer.network.exceptions

sealed class EmailValidationException(
    requestContextId: String,
) : ApiException.ApiV2ErrorException(
    code = "",
    description = "",
    requestContextId = requestContextId
) {

    /** Invalid email validation code. */
    class InvalidCode(requestContextId: String) : EmailValidationException(requestContextId = requestContextId)
    class CodeExpired(requestContextId: String) : EmailValidationException(requestContextId = requestContextId)
    class TooManyAttempts(requestContextId: String) : EmailValidationException(requestContextId = requestContextId)
}

internal fun ApiException.ApiV2ErrorException.toEmailValidationErrorsException() = when (code) {
    "email_validation_request_locked", "email_validation_rate_limit_error" -> {
        EmailValidationException.TooManyAttempts(requestContextId)
    }
    "email_validation_token_expired" -> EmailValidationException.CodeExpired(requestContextId)
    "invalid_validation_token" -> EmailValidationException.InvalidCode(requestContextId)
    else -> this
}
