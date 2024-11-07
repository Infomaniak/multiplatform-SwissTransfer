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
package com.infomaniak.multiplatform_swisstransfer.common.models.files

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class DisplayableFile(
    val id: String = Uuid.random().toString(),
    val name: String,
    val isFolder: Boolean,
    var children: MutableList<DisplayableFile> = mutableListOf(),
    var parent: DisplayableFile? = null,
    val url: String? = null,
    val size: Long? = null
) {

    // Init for folder
    constructor(folderName: String) : this(
        name = folderName,
        isFolder = true,
    )

    constructor(uploadFile: UploadFile) : this(
        id = uploadFile.url,
        name = uploadFile.url.substringAfterLast("/"),
        url = uploadFile.url,
        isFolder = false,
    )

    /**
     * Recursively searches for a child with the given name.
     *
     * @param targetName The name of the child to search for.
     * @return The [DisplayableFile] object representing the found child, or null if not found.
     */
    fun findChildByName(targetName: String): DisplayableFile? {
        if (name == targetName) return this

        children.forEach { child ->
            child.findChildByName(targetName)?.let {
                return it
            }
        }

        return null
    }

    private fun treeLines(): List<String> {
        return listOf(name) + children.flatMap { it.treeLines() }.map { "        $it" }
    }

    fun printTree() {
        println(treeLines().joinToString("\n"))
    }

    override fun toString() = "$name -> children: ${children.map { it.name }}"
}
