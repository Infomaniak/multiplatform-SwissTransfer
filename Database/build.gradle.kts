import co.touchlab.skie.configuration.DefaultArgumentInterop
import com.infomaniak.multiplatform_swisstranfer.gradle.PublishExtension.Companion.publishConfig
import com.infomaniak.multiplatform_swisstranfer.gradle.PublishPlugin
import com.infomaniak.multiplatform_swisstranfer.utils.Versions
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.realm)
    alias(libs.plugins.skie)
}
apply<PublishPlugin>()

publishConfig {
    mavenName = "database"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Versions.javaVersion.toString()))
        }
        publishLibraryVariants("release")
    }

    val xcframeworkName = "Database"
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
            DefaultArgumentInterop.MaximumDefaultArgumentCount(Versions.skieMaxArgumentCount)
        }
    }
    build {
        produceDistributableFramework()
    }
}

android {
    namespace = "com.infomaniak.multiplatform_swisstransfer.database"
    compileSdk = Versions.compileSdk
    defaultConfig {
        minSdk = Versions.minSdk
    }
    compileOptions {
        sourceCompatibility = Versions.javaVersion
        targetCompatibility = Versions.javaVersion
    }
}
