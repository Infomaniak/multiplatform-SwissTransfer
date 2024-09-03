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
package com.infomaniak.gradle.plugins

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

open class STMultiplatformExtension {
    var appleExportedProjects = listOf<Project>()

    companion object {
        internal const val EXTENSION_NAME = "kotlinMultiplatformConfig"

        fun Project.kotlinMultiplatformConfig(configure: Action<STMultiplatformExtension>): Unit =
            (this as ExtensionAware).extensions.configure(EXTENSION_NAME, configure)
    }
}
