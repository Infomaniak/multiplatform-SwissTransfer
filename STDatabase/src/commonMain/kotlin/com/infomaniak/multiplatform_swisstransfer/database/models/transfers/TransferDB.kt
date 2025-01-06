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
package com.infomaniak.multiplatform_swisstransfer.database.models.transfers

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.upload.UploadSession
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferStatus
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Clock

class TransferDB() : Transfer, RealmObject {
    @PrimaryKey
    override var linkUUID: String = ""
    override var containerUUID: String = ""
    override var downloadCounterCredit: Int = 0
    override var createdDateTimestamp: Long = 0L
    override var expiredDateTimestamp: Long = 0L
    override var hasBeenDownloadedOneTime: Boolean = false
    override var isMailSent: Boolean = false
    override var downloadHost: String = ""
    override var container: ContainerDB? = null
    override var password: String? = null
    override var recipients: RealmSet<String> = realmSetOf()

    private var transferDirectionValue: String = ""
    private var transferStatusValue: String = TransferStatus.UNKNOWN.name

    @Ignore
    override val transferDirection: TransferDirection get() = TransferDirection.valueOf(transferDirectionValue)
    @Ignore
    override val transferStatus: TransferStatus get() = TransferStatus.valueOf(transferStatusValue)

    constructor(transfer: Transfer, transferDirection: TransferDirection, password: String?) : this() {
        this.linkUUID = transfer.linkUUID
        this.containerUUID = transfer.containerUUID
        this.downloadCounterCredit = transfer.downloadCounterCredit
        this.createdDateTimestamp = transfer.createdDateTimestamp
        this.expiredDateTimestamp = transfer.expiredDateTimestamp
        this.hasBeenDownloadedOneTime = transfer.hasBeenDownloadedOneTime
        this.isMailSent = transfer.isMailSent
        this.downloadHost = transfer.downloadHost
        this.container = transfer.container?.let(::ContainerDB)
        this.password = password ?: transfer.password

        this.transferDirectionValue = transferDirection.name
        this.transferStatusValue = transfer.transferStatus?.name ?: TransferStatus.READY.name
    }

    constructor(linkUUID: String, uploadSession: UploadSession, transferStatus: TransferStatus) : this() {
        this.linkUUID = linkUUID
        this.containerUUID = uploadSession.remoteContainer!!.uuid
        this.downloadCounterCredit = uploadSession.remoteContainer!!.downloadLimit
        this.createdDateTimestamp = Clock.System.now().epochSeconds
        this.expiredDateTimestamp = uploadSession.remoteContainer!!.expiredDateTimestamp
        this.hasBeenDownloadedOneTime = false
        this.isMailSent = false
        this.downloadHost = ""
        this.container = ContainerDB(uploadSession.remoteContainer!!, uploadSession.files)
        this.password = uploadSession.password.ifEmpty { null }
        this.recipients = uploadSession.recipientsEmails.mapTo(destination = realmSetOf(), transform = { it })

        this.transferDirectionValue = TransferDirection.SENT.name
        this.transferStatusValue = transferStatus.name
    }

    internal companion object {
        val transferDirectionPropertyName = TransferDB::transferDirectionValue.name
        val transferStatusPropertyName = TransferDB::transferStatusValue.name
    }
}
