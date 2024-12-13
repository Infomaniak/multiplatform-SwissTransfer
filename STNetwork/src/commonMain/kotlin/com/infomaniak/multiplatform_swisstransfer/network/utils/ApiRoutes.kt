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
package com.infomaniak.multiplatform_swisstransfer.network.utils

internal object ApiRoutes {

    private const val PROD_URL = "https://www.swisstransfer.com"
    private const val PREPROD_BASE_URL = "https://swisstransfer.preprod.dev.infomaniak.ch"
    const val BASE_URL = PREPROD_BASE_URL
    const val API_BASE_URL = "$BASE_URL/api/"

    //region Transfer
    fun getTransfer(linkUUID: String): String = "links/$linkUUID"
    //endRegion

    //region Upload
    const val initUpload = "mobile/containers"
    const val verifyEmailCode = "emails-validation"
    const val resendEmailCode = "$verifyEmailCode/resend"
    const val finishUpload = "uploadComplete"
    const val cancelUpload = "cancelUpload"
    //endregion
}
