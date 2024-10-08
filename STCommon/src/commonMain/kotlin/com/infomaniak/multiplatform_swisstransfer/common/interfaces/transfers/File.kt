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
    var containerUuid: String
    var uuid: String
    var fileName: String
    var fileSizeInBytes: Long
    var downloadCounter: Int
    var createdDateTimestamp: Long
    var expiredDateTimestamp: Long
    var eVirus: String
    var deletedDate: String?
    var mimeType: String
    var receivedSizeInBytes: Long
    var path: String?
    var thumbnailPath: String?
}
