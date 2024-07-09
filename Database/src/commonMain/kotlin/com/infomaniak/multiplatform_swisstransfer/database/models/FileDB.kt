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

package com.infomaniak.multiplatform_swisstransfer.database.models

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.File
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class FileDB : File, RealmObject {
    @PrimaryKey
    override var containerUUID: String = ""
    override var uuid: String = ""
    override var fileName: String = ""
    override var fileSizeInBytes: Long = 0
    override var downloadCounter: Long = 0
    override var createdDateTimestamp: Long = 0
    override var expiredDateTimestamp: Long = 0
    override var eVirus: String = ""
    override var deletedDate: String? = null
    override var mimeType: String = ""
    override var receivedSizeInBytes: Long = 0
    override var path: String? = ""
}
