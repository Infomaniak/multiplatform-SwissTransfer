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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Container
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.File
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer

object DummyTransfer {

    private val container1 = object : Container {
        override var uuid: String = "transfer1Container"
        override var duration: Long = 0L
        override var createdDateTimestamp: Long = 0L
        override var expiredDateTimestamp: Long = 0L
        override var numberOfFiles: Int = 0
        override var message: String? = null
        override var needPassword: Boolean = false
        override var language: String = "language"
        override var sizeUploaded: Long = 0L
        override var deletedDateTimestamp: Long? = null
        override var swiftVersion: Int = 0
        override var downloadLimit: Int = 0
        override var source: String = "source"
        override var files: List<File> = emptyList()
    }

    val container2 = object : Container by container1 {
        override val uuid: String = "transfer2container"
    }

    val transfer1 = object : Transfer {
        override var linkUuid: String = "transferLinkUuid1"
        override var containerUuid: String = "containerUuid"
        override var downloadCounterCredit: Int = 0
        override var createdDateTimestamp: Long = 0L
        override var expiredDateTimestamp: Long = 0L
        override var hasBeenDownloadedOneTime: Boolean = false
        override var isMailSent: Boolean = true
        override var downloadHost: String = "url"
        override var container: Container = this@DummyTransfer.container1
    }

    val transfer2 = object : Transfer by transfer1 {
        override val linkUuid: String = "transfer2"
        override val containerUuid: String = "container2"
        override val container: Container = container2
    }

    val transfers = listOf(transfer1, transfer2)

}
