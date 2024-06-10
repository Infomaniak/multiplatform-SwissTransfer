import co.touchlab.skie.configuration.DefaultArgumentInterop
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.realm)
    alias(libs.plugins.skie)
}

val sharedMinSdk: Int by rootProject.extra
val sharedCompileSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra
val skieMaxArgumentCount: Int by rootProject.extra

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    val xcframeworkName = "DB"
    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = xcframeworkName
            binaryOption("bundleId", "com.infomaniak.multiplatform_swisstransfer.${xcframeworkName}")
            xcf.add(this)
            isStatic = true
        }
    }

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

skie {
    features {
        group {
            DefaultArgumentInterop.Enabled(true)
            DefaultArgumentInterop.MaximumDefaultArgumentCount(skieMaxArgumentCount)
        }
    }
    build {
        produceDistributableFramework()
    }
}

android {
    namespace = "com.infomaniak.multiplatform_swisstransfer.db"
    compileSdk = sharedCompileSdk
    defaultConfig {
        minSdk = sharedMinSdk
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}
