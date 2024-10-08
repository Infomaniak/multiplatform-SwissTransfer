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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Container
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ContainerDB : Container<RealmList<FileDB>>, RealmObject {
    @PrimaryKey
    override var uuid: String = ""
    override var duration: Long = 0L
    override var createdDateTimestamp: Long = 0L
    override var expiredDateTimestamp: Long = 0L
    override var numberOfFiles: Int = 0
    override var message: String? = ""
    override var needPassword: Boolean = false
    override var language: String = ""
    override var sizeUploaded: Long = 0L
    override var deletedDateTimestamp: Long? = null
    override var swiftVersion: Int = 0
    override var downloadLimit: Int = 0
    override var source: String = ""
    // @SerialName("WSUser") // TODO: What's this ?
    // val wsUser: JsonElement?
    override var files: RealmList<FileDB> = realmListOf()
}
