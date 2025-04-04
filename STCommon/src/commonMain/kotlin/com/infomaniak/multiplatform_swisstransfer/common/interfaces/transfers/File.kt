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
package com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers

interface File {
    val containerUUID: String
    val uuid: String
    val fileName: String
    val isFolder: Boolean get() = false
    val fileSizeInBytes: Long
    val downloadCounter: Int
    val createdDateTimestamp: Long
    val expiredDateTimestamp: Long
    val eVirus: String
    val deletedDate: String?
    val mimeType: String?
    val receivedSizeInBytes: Long
    val path: String?
    val thumbnailPath: String?
}
