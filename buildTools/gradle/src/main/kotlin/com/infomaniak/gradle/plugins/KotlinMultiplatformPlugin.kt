package com.infomaniak.gradle.plugins

import com.infomaniak.gradle.extensions.configureAndroid
import com.infomaniak.gradle.extensions.configureKotlinMultiplatform
import com.infomaniak.gradle.extensions.configureSkie
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

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

class KotlinMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        extensions.apply {
            configure(::configureKotlinMultiplatform)
            configure(::configureAndroid)
            if (plugins.hasPlugin("skie")) configure(::configureSkie)
        }
    }
}
