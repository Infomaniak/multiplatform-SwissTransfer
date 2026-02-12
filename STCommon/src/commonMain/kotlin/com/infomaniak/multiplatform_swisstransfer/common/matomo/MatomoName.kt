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

enum class MatomoName(val value: String) {

    //region Common
    AddAccount("addAccount"),
    ConsultOneFile("consultOneFile"),
    CopyLink("copyLink"),
    CopyPassword("copyPassword"),
    Dark("dark"),
    DeleteFile("deleteFile"),
    DeleteMyAccount("deleteMyAccount"),
    DiscoverInfomaniak("discoverInfomaniak"),
    DiscoverLater("discoverLater"),
    DiscoverNow("discoverNow"),
    DownloadTransfer("downloadTransfer"),
    English("english"),
    ExpiredDate("expiredDate"),
    ExpiredDownloads("expiredDownloads"),
    FifteenDays("15days"),
    French("french"),
    German("german"),
    GiveYourOpinion("giveYourOpinion"),
    HelpAndSupport("helpAndSupport"),
    Italian("italian"),
    Light("light"),
    Link("link"),
    Login("login"),
    Logout("logout"),
    Mail("mail"),
    OneDay("1day"),
    OneDownload("1download"),
    OneHundredDownloads("100downloads"),
    SevenDays("7days"),
    Share("share"),
    ShareYourIdeas("shareYourIdeas"),
    ShowPassword("showPassword"),
    ShowQRCode("showQRCode"),
    Spanish("spanish"),
    Switch("switch"),
    SwitchUser("switchUser"),
    System("system"),
    TermsAndConditions("termsAndConditions"),
    ThirtyDays("30days"),
    TogglePassword("togglePassword"),
    TwentyDownloads("20downloads"),
    TwoHundredAndFiftyDownloads("250downloads"),
    VirusDetected("virusDetected"),
    //endregion

    //region Android
    Notifications("notifications"),
    //endregion

    //region iOS
    AddFromCamera("addFromCamera"),
    AddFromDocumentPicker("addFromDocumentPicker"),
    AddFromGallery("addFromGallery"),
    //endregion
}
