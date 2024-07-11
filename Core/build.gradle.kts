plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.skie)
    id("infomaniak.kotlinMultiplatform")
    id("infomaniak.PublishPlugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":Common"))
            implementation(project(":Database"))
            implementation(project(":Network"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
