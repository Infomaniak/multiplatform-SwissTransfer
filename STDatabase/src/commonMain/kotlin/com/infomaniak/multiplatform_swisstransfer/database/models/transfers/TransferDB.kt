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
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class TransferDB : Transfer<ContainerDB?>, RealmObject {
    @PrimaryKey
    override var linkUUID: String = ""
    override var containerUUID: String = ""
    override var downloadCounterCredit: Long = 0L
    override var createdDateTimestamp: Long = 0L
    override var expiredDateTimestamp: Long = 0L
    override var isDownloadOnetime: Long = 0L // TODO: Boolean ?
    override var isMailSent: Boolean = false
    override var downloadHost: String = ""
    override var container: ContainerDB? = null
}
