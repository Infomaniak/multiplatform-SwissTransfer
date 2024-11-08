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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.ui.FileUi

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
     * @param targetName The name of the child to search for.
     * @return The [File] object representing the found child, or null if not found.
     */
    fun findChildByName(targetName: String): File? {
        if (fileName == targetName) return this

        children.forEach { child ->
            child.findChildByName(targetName)?.let {
                return it
            }
        }

        return null
    }

    fun treeLines(): List<String> {
        return listOf(fileName) + children.flatMap { it.treeLines() }.map { "        $it" }
    }
}
