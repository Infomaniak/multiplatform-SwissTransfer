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
include(":Common")
include(":Network")
include(":Database")
include(":Core")
includeBuild("buildTools")
