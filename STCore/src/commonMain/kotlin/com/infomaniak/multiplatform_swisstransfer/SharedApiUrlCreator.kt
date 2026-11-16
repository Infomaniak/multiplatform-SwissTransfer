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
package com.infomaniak.multiplatform_swisstransfer

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.TransferUi
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.utils.SharedApiV2Routes
import io.ktor.http.URLBuilder
import kotlin.coroutines.cancellation.CancellationException

/**
 * Utility class responsible for creating API URLs for shared routes.
 */
class SharedApiUrlCreator internal constructor(private val environment: ApiEnvironment) {

    /**
     * @see com.infomaniak.multiplatform_swisstransfer.managers.UploadV2Manager.finalizeTransferAndGetLinkUuid
     */
    fun shareTransferV2Url(linkUUID: String) = SharedApiV2Routes.shareTransfer(environment, linkUUID)

    @Throws(CancellationException::class)
    fun downloadFileUrl(transfer: TransferUi, fileUUID: String): String = SharedApiV2Routes.downloadFile(
        environment = environment,
        linkId = transfer.linkId!!, // Should never be null for non-pending upload v2 transfers
        fileId = fileUUID
    )

    fun reportTransferUrl(transfer: TransferUi): String {
        val transferUrl = shareTransferV2Url(
            linkUUID = requireNotNull(transfer.linkId) {
                "A received v2 transfer must have a link ID"
            }
        )

        return URLBuilder("https://www.infomaniak.com/gtl/report_abuse").apply {
            parameters.append("from", "swisstransfer")
            parameters.append("url", transferUrl)
        }.buildString()
    }
}
