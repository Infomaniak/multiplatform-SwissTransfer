enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "multiplatform-SwissTransfer"
include(":STCommon")
include(":STNetwork")
include(":STDatabase")
include(":STCore")
includeBuild("buildTools")
