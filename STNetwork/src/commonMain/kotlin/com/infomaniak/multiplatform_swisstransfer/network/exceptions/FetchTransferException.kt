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

import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException

/**
 * A sealed class representing exceptions related to fetched transfers to extend [ApiException.ApiErrorException].
 * This class is used to handle specific errors that occur when fetching a transfer.
 *
 * @property statusCode The HTTP status code associated with the error.
 * @property message The message describing the error.
 * @constructor Creates an [FetchTransferException] with the given status code.
 *
 * @param statusCode The HTTP status code for the error.
 */
sealed class FetchTransferException(
    statusCode: Int,
    message: String,
    requestContextId: String,
) : ApiErrorException(statusCode, message, requestContextId) {

    class VirusCheckFetchTransferException(requestContextId: String) :
        FetchTransferException(404, "Virus check in progress", requestContextId)

    class VirusDetectedFetchTransferException(requestContextId: String) :
        FetchTransferException(404, "Virus has been detected", requestContextId)

    class ExpiredDateFetchTransferException(requestContextId: String) :
        FetchTransferException(404, "Transfer expired", requestContextId)

    class NotFoundFetchTransferException(requestContextId: String) :
        FetchTransferException(404, "Transfer not found", requestContextId)

    class PasswordNeededFetchTransferException(requestContextId: String) :
        FetchTransferException(401, "Transfer need a password", requestContextId)

    class WrongPasswordFetchTransferException(requestContextId: String) :
        FetchTransferException(401, "Wrong password for this Transfer", requestContextId)

    companion object {

        private const val ERROR_VIRUS_CHECK = "wait_virus_check"
        private const val ERROR_VIRUS_DETECTED = "virus has been detected"
        private const val ERROR_EXPIRED = "expired"
        private const val ERROR_NEED_PASSWORD = "need_password"
        private const val ERROR_WRONG_PASSWORD = "wrong_password"

        /**
         * Extension function to convert an instance of [ApiException.UnexpectedApiErrorFormatException] to a specific
         * [FetchTransferException] based on its error message.
         *
         * @receiver An instance of [ApiException.UnexpectedApiErrorFormatException].
         * @return An instance of [FetchTransferException] or the original [ApiException.UnexpectedApiErrorFormatException]
         * if we cannot map it to a [FetchTransferException].
         */
        fun UnexpectedApiErrorFormatException.toFetchTransferException() = when {
            message?.contains(ERROR_VIRUS_CHECK) == true -> VirusCheckFetchTransferException(requestContextId)
            message?.contains(ERROR_VIRUS_DETECTED) == true -> VirusDetectedFetchTransferException(requestContextId)
            message?.contains(ERROR_EXPIRED) == true -> ExpiredDateFetchTransferException(requestContextId)
            statusCode == 404 -> NotFoundFetchTransferException(requestContextId)
            message?.contains(ERROR_NEED_PASSWORD) == true -> PasswordNeededFetchTransferException(requestContextId)
            message?.contains(ERROR_WRONG_PASSWORD) == true -> WrongPasswordFetchTransferException(requestContextId)
            else -> this
        }
    }
}

class DownloadQuotaExceededException : Exception("Transfer download quota exceeded")
