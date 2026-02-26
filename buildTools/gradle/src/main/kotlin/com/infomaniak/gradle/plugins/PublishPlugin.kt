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
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

class PublishPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.plugins.apply(SigningPlugin::class.java)
        target.plugins.apply("maven-publish")
        target.plugins.apply("com.gradleup.nmcp")

        // Create the PublishExtension and add it to the project
        val extension = target.extensions.create<PublishExtension>(PublishExtension.EXTENSION_NAME)

        target.group = "com.infomaniak.multiplatform_swisstransfer"
        target.version = Versions.mavenVersionName

        target.afterEvaluate {
            val mavenName = extension.mavenName ?: target.name

            target.extensions.configure<PublishingExtension> {
                publications {
                    withType<MavenPublication> {
                        pom {
                            name.set(mavenName)
                            description.set("Multiplatform SwissTransfer - $mavenName")
                            licenses {
                                license {
                                    name.set("GPL-3.0")
                                    url.set("https://www.gnu.org/licenses/gpl-3.0.fr.html")
                                }
                            }
                            url.set("https://github.com/Infomaniak/multiplatform-SwissTransfer")
                            issueManagement {
                                system.set("Github")
                                url.set("https://github.com/Infomaniak/multiplatform-SwissTransfer/issues")
                            }
                            scm {
                                connection.set("https://github.com/Infomaniak/multiplatform-SwissTransfer.git")
                                url.set("https://github.com/Infomaniak/multiplatform-SwissTransfer")
                            }
                            organization {
                                name.set("Infomaniak Network SA")
                                url.set("https://www.infomaniak.com/")
                            }
                            developers {
                                developer {
                                    id.set("Infomaniak")
                                    email.set("mobile+libraries@infomaniak-dev.ch")
                                    name.set("Infomaniak Development Team")
                                    url.set("https://www.infomaniak.com/")
                                }
                            }
                        }
                    }
                }
            }

            target.extensions.configure<SigningExtension> {
                val keyId: String = getPropertyValue(project, "GPG_key_id") ?: return@configure
                val ringFile: String = getPropertyValue(project, "GPG_private_key")?.replace('#', '\n') ?: return@configure
                val password: String = getPropertyValue(project, "GPG_private_password") ?: return@configure

                isRequired = true
                useInMemoryPgpKeys(keyId, ringFile, password)
                sign(project.extensions.getByType<PublishingExtension>().publications)

                // Workaround for a Gradle bug, the issue is still open.
                // https://github.com/gradle/gradle/issues/26091#issuecomment-1722947958
                tasks.withType<AbstractPublishToMaven>().configureEach {
                    val signingTasks = tasks.withType<Sign>()
                    mustRunAfter(signingTasks)
                }
            }
        }
    }
}

private fun getPropertyValue(project: Project, propertyName: String): String? {
    if (project.hasProperty(propertyName)) return project.property(propertyName) as String

    val systemValue: String? = System.getenv(propertyName)
    return systemValue
}
