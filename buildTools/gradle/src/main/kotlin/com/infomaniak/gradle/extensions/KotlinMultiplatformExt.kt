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

package com.infomaniak.gradle.extensions

import com.infomaniak.gradle.plugins.STMultiplatformExtension
import com.infomaniak.gradle.utils.Versions
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

internal fun Project.configureKotlinMultiplatform(extension: KotlinMultiplatformExtension) = extension.apply {
    // Create the STMultiplatformExtension and add it to the project
    val stMultiplatformExtension = extensions.create<STMultiplatformExtension>(STMultiplatformExtension.EXTENSION_NAME)

    // Do not generate source code for all platforms, only for Android
    withSourcesJar(publish = false)
    // Targets
    androidTarget {
        withSourcesJar(publish = true)
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Versions.javaVersion.toString()))
        }
        publishLibraryVariants("release")
    }

    val xcframeworkName = name
    val xcf = XCFramework()
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = xcframeworkName
            binaryOption("bundleId", "com.infomaniak.multiplatform_swisstransfer.${xcframeworkName}")
            xcf.add(this)
            isStatic = true

            afterEvaluate {
                stMultiplatformExtension.appleExportedProjects.forEach { stProject ->
                    export(stProject)
                }
            }
        }
    }

    // Provide documentation with kDoc in Objective-C header
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexport-kdoc")
    }
}
