// settings.gradle.kts
pluginManagement {
    repositories {
        maven { url = uri("https://maven.myket.ir") }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.myket.ir") }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SafarBan"
include(":app")
include(":persiandatepicker")
