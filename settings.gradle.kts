pluginManagement {
    repositories {
        maven("https://maven.myket.ir")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.myket.ir")
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}


rootProject.name = "SafarBan"
include(":app")
 