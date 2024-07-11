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

plugins {
    `kotlin-dsl`
}

group = "com.infomaniak.buildTools"

gradlePlugin {
    plugins {
        register("infomaniak.kotlinMultiplatform") {
            id = "infomaniak.kotlinMultiplatform"
            implementationClass = "com.infomaniak.gradle.plugins.KotlinMultiplatformPlugin"
        }

        register("PublishPlugin") {
            id = "infomaniak.PublishPlugin"
            implementationClass = "com.infomaniak.gradle.plugins.PublishPlugin"
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.skie.gradle.plugin)
}