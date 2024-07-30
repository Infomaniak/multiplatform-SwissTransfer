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

import com.infomaniak.gradle.utils.Versions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

class PublishPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.plugins.apply("maven-publish")

        // Create the PublishExtension and add it to the project
        val extension = target.extensions.create<PublishExtension>(PublishExtension.EXTENSION_NAME)

        target.group = "com.github.infomaniak.multiplatform_swisstransfer"
        target.version = Versions.mavenVersionName

        target.afterEvaluate {
            val mavenName = extension.mavenName ?: target.name.lowercase()

            target.extensions.configure<org.gradle.api.publish.PublishingExtension> {
                publications {
                    create<MavenPublication>("android") {
                        from(components.findByName("release"))
                        groupId = "com.github.infomaniak.multiplatform_swisstransfer"
                        artifactId = mavenName
                        version = Versions.mavenVersionName
                    }
                }
            }
        }
    }
}
