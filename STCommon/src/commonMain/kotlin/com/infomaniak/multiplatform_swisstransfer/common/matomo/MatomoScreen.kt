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
    DateExpiredTransfer("DateExpiredTransfer"),
    DownloadLimitSetting("DownloadLimitSetting"),
    DownloadQuotasExpiredTransfer("DownloadQuotasExpiredTransfer"),
    EmailLanguageSetting("EmailLanguageSetting"),
    NewTransfer("NewTransfer"),
    NewTransferFileList("NewTransferFileList"),
    NotificationsSettings("NotificationsSettings"),
    Received("Received"),
    ReceivedTransferDetails("ReceivedTransferDetails"),
    Sent("Sent"),
    SentTransferDetails("SentTransferDetails"),
    Settings("Settings"),
    ScreenshotQrCode("ScreenshotQrCode"),
    ThemeSetting("ThemeSetting"),
    TransferDetailsFileList("TransferDetailsFileList"),
    UploadError("UploadError"),
    UploadProgress("UploadProgress"),
    UploadSuccess("UploadSuccess"),
    ValidityPeriodSetting("ValidityPeriodSetting"),
    VerifyMail("VerifyMail"),
    VirusCheck("VirusCheck"),
    VirusDetected("VirusDetected"),
}
