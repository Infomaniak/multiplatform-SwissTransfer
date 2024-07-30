plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.skie)
    id("infomaniak.kotlinMultiplatform")
    id("infomaniak.publishPlugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":STCommon"))
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negociation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.encoding)
            api(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
