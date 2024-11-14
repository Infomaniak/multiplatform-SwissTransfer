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
    val isFolder: Boolean
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
    val parent: File?
    val children: List<File>

    /**
     * Recursively searches for a child with the given name.
     *
     * @param fileName The name of the child to search for.
     * @return The [File] object representing the found child, or null if not found.
     */
    fun findChildByName(fileName: String): File? {
        if (this.fileName == fileName) return this

        children.forEach { child ->
            child.findChildByName(fileName)?.let {
                return it
            }
        }

        return null
    }

    /**
     * Recursively searches for a child with the given UUID.
     *
     * @param uuid The UUID of the child to search for.
     * @return The [File] object representing the found child, or null if not found.
     */
    fun findChildByUuid(uuid: String): File? {
        if (this.uuid == uuid) return this

        children.forEach { child ->
            child.findChildByUuid(uuid)?.let {
                return it
            }
        }

        return null
    }

    companion object {

        fun List<File>.findFirstChildByUuid(uuid: String): File? {
            forEach { file ->
                val foundChild = file.findChildByUuid(uuid)
                if (foundChild != null) {
                    return foundChild
                }
            }
            return null
        }
    }
}
