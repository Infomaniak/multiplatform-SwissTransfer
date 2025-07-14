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
package com.infomaniak.multiplatform_swisstransfer.common.matomo

enum class MatomoScreen(val value: String) {
    DownloadLimitSetting("downloadLimitSetting"),
    EmailLanguageSetting("emailLanguageSetting"),
    NewTransfer("newTransfer"),
    NewTransferFileList("newTransferFileList"),
    Received("received"),
    ReceivedTransferDetails("receivedTransferDetails"),
    Sent("sent"),
    SentTransferDetails("sentTransferDetails"),
    Settings("settings"),
    ThemeSetting("themeSetting"),
    TransferDetailsFileList("transferDetailsFileList"),
    UploadError("uploadError"),
    UploadProgress("uploadProgress"),
    UploadSuccess("uploadSuccess"),
    ValidityPeriodSetting("validityPeriodSetting"),
    VerifyMail("verifyMail"),
}
