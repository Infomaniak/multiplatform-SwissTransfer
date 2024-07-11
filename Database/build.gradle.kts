
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.realm)
    alias(libs.plugins.skie)
    id("infomaniak.kotlinMultiplatform")
    id("infomaniak.PublishPlugin")
}

//publishConfig {
//    mavenName = "database"
//}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":Common"))
            implementation(libs.realm.base)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
