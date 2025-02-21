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
 * A sealed class representing exceptions related to fetched transfers to extend [ApiException].
 * This class is used to handle specific errors that occur when fetching a transfer.
 *
 * @property statusCode The HTTP status code associated with the error.
 * @property message The message describing the error.
 * @constructor Creates an [FetchTransferException] with the given status code.
 *
 * @param statusCode The HTTP status code for the error.
 */
sealed class FetchTransferException(statusCode: Int, override val message: String) : ApiException(statusCode, message) {

    class VirusCheckFetchTransferException: FetchTransferException(404, "Virus check in progress")

    class VirusDetectedFetchTransferException: FetchTransferException(404, "Virus has been detected")

    class ExpiredDateFetchTransferException : FetchTransferException(404, "Transfer expired")

    class NotFoundFetchTransferException : FetchTransferException(404, "Transfer not found")

    class PasswordNeededFetchTransferException : FetchTransferException(401, "Transfer need a password")

    class WrongPasswordFetchTransferException : FetchTransferException(401, "Wrong password for this Transfer")

    companion object {

        private const val ERROR_VIRUS_CHECK = "wait_virus_check"
        private const val ERROR_VIRUS_DETECTED = "virus has been detected"
        private const val ERROR_EXPIRED = "expired"
        private const val ERROR_NEED_PASSWORD = "need_password"
        private const val ERROR_WRONG_PASSWORD = "wrong_password"

        /**
         * Extension function to convert an instance of [UnexpectedApiErrorFormatException] to a specific
         * [FetchTransferException] based on its error message.
         *
         * @receiver An instance of [UnexpectedApiErrorFormatException].
         * @return An instance of [FetchTransferException] or the original [UnexpectedApiErrorFormatException]
         * if we cannot map it to a [FetchTransferException].
         */
        fun UnexpectedApiErrorFormatException.toFetchTransferException() = when {
            message?.contains(ERROR_VIRUS_CHECK) == true -> VirusCheckFetchTransferException()
            message?.contains(ERROR_VIRUS_DETECTED) == true -> VirusDetectedFetchTransferException()
            message?.contains(ERROR_EXPIRED) == true -> ExpiredDateFetchTransferException()
            statusCode == 404 -> NotFoundFetchTransferException()
            message?.contains(ERROR_NEED_PASSWORD) == true -> PasswordNeededFetchTransferException()
            message?.contains(ERROR_WRONG_PASSWORD) == true -> WrongPasswordFetchTransferException()
            else -> this
        }
    }
}

class ExpiredDownloadFetchTransferException : Exception("Transfer Download quota expired")