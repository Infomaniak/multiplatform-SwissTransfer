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
package com.infomaniak.multiplatform_swisstransfer.database.dataset

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.Upload
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadContainer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadFile

object DummyUpload {
    private val container1 = object : UploadContainer {
        override val uuid: String = "uploadContainer1"
        override val duration: String = "30"
        override val downloadLimit: Long = 1
        override val language: String = "Fr-fr"
        override val source: String = "ST"
        override val wsUser: String? = null
        override val authorIP: String = ""
        override val swiftVersion: String = ""
        override val expiredDateTimestamp: Long = 0
        override val needPassword: Boolean = false
        override val message: String = ""
        override val numberOfFiles: Int = 0
    }
    private val container2 = object : UploadContainer by container1 {
        override val uuid: String = "container2"
    }

    private val upload1 = object : Upload {
        override val uuid: String = "upload1"
        override val container: UploadContainer = container1
        override val uploadHost: String = ""
        override val files: List<UploadFile> = emptyList()
    }

    private val upload2 = object : Upload by upload1 {
        override val uuid: String = "upload2"
        override val container: UploadContainer = container2
    }

    val uploads = listOf(upload1, upload2)
}
