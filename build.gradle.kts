buildscript {
    extra.apply {
        set("sharedMinSdk", 24)
        set("sharedCompileSdk", 34)
        set("javaVersion", JavaVersion.VERSION_17)
    }
}

plugins {
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.realm).apply(false)
}
