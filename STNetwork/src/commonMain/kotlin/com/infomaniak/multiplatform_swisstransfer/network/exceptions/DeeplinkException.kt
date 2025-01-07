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
 * A sealed class representing exceptions related to deep link's opening to extend [ApiException].
 * This class is used to handle specific errors that occur during the email validation process.
 *
 * @property statusCode The HTTP status code associated with the error.
 * @property message The message describing the error.
 * @constructor Creates an [DeeplinkException] with the given status code.
 *
 * @param statusCode The HTTP status code for the error.
 */
sealed class DeeplinkException(statusCode: Int, override val message: String) : ApiException(statusCode, message) {

    class PasswordNeededDeeplinkException : DeeplinkException(401, "Transfer need a password")

    class WrongPasswordDeeplinkException : DeeplinkException(401, "Wrong password for this Transfer")

    internal companion object {

        private const val ERROR_NEED_PASSWORD = "need_password"
        private const val ERROR_WRONG_PASSWORD = "wrong_password"

        /**
         * Extension function to convert an instance of [UnexpectedApiErrorFormatException] to a specific
         * [DeeplinkException] based on its error message.
         *
         * @receiver An instance of [UnexpectedApiErrorFormatException].
         * @return An instance of [DeeplinkException] which can be [PasswordNeededDeeplinkException]
         * or the original [UnexpectedApiErrorFormatException] if the error message does not match any predefined values.
         */
        fun UnexpectedApiErrorFormatException.toDeeplinkException() = when {
            message?.contains(ERROR_NEED_PASSWORD) == true -> PasswordNeededDeeplinkException()
            message?.contains(ERROR_WRONG_PASSWORD) == true -> WrongPasswordDeeplinkException()
            else -> this
        }
    }
}
