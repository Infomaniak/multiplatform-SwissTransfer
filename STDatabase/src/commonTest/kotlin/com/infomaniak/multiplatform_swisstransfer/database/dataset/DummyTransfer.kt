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
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

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
        override var files: List<File> = listOf(
            createDummyFile(containerUUID = uuid, fileName = "cat no no.gif", mimeType = "image/gif"),
            createDummyFile(containerUUID = uuid, fileName = "cat vibe.gif", mimeType = "image/gif"),
        )
    }

    val container2 = object : Container by container1 {
        override val uuid: String = "transfer2container"
    }

    val expired: Transfer
    val notExpired: Transfer

    val transfer1 = object : Transfer {
        override var linkUUID: String = "transferLinkUUID1"
        override var containerUUID: String = "containerUUID"
        override var downloadCounterCredit: Int = 0
        override var createdDateTimestamp: Long = 0L
        override var expiredDateTimestamp: Long = 1_730_458_842L // 01/11/2024
        override var hasBeenDownloadedOneTime: Boolean = false
        override var isMailSent: Boolean = true
        override var downloadHost: String = "url"
        override var container: Container = this@DummyTransfer.container1
        override val transferStatus: TransferStatus = TransferStatus.READY
    }

    val transfer2 = object : Transfer by transfer1 {
        override val linkUUID: String = "transfer2"
        override val containerUUID: String = "container2"
        override var expiredDateTimestamp: Long = 4_102_441_200L // 01/01/2100
        override val container: Container = container2
        override val transferStatus: TransferStatus = TransferStatus.WAIT_VIRUS_CHECK
    }

    init {
        expired = transfer1
        notExpired = transfer2
    }

    val transfers = listOf(transfer1, transfer2)

    private fun createDummyFile(
        containerUUID: String,
        fileName: String,
        mimeType: String? = null,
    ): File = object : File {
        override val containerUUID: String = containerUUID
        override val uuid: String = "$fileName|whatever"
        override val fileName: String = fileName
        override val fileSizeInBytes: Long = 10_000_000L
        override val downloadCounter: Int = 0
        override val createdDateTimestamp: Long = (Clock.System.now() - 1.hours).epochSeconds
        override val expiredDateTimestamp: Long = (Clock.System.now() + 7.days).epochSeconds
        override val eVirus: String = ""
        override val deletedDate: String? = null
        override val mimeType: String? = mimeType
        override val receivedSizeInBytes: Long = fileSizeInBytes
        override val path: String? = null
        override val thumbnailPath: String? = null

    }

}
