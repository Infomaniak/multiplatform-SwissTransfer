plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.skie)
    id("infomaniak.kotlinMultiplatform")
    id("infomaniak.publishPlugin")
}

private val commonProject = project(":STCommon")

kotlinMultiplatformConfig {
    appleExportedProjects = listOf(commonProject)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(commonProject)
            api(libs.ktor.client.core)
            implementation(project(":STDatabase"))
            implementation(project(":STNetwork"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
