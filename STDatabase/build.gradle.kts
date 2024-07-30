plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.realm)
    alias(libs.plugins.skie)
    id("infomaniak.kotlinMultiplatform")
    id("infomaniak.publishPlugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":STCommon"))
            implementation(libs.realm.base)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
