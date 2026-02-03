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

sealed class UploadErrorsException : ApiException.ApiV2ErrorException(code = "", description = "", requestContextId = "") {

    class NotFoundException() : UploadErrorsException()
    class TransferCancelled() : UploadErrorsException()
    class TransferExpired() : UploadErrorsException()
    class TransferFailed() : UploadErrorsException()
}

internal fun ApiException.ApiV2ErrorException.toUploadErrorsException() = when (code) {
    "object_not_found" -> UploadErrorsException.NotFoundException()
    "transfer_cancelled" -> UploadErrorsException.TransferCancelled()
    "transfer_expired" -> UploadErrorsException.TransferExpired()
    "transfer_failed" -> UploadErrorsException.TransferFailed()
    else -> this
}
