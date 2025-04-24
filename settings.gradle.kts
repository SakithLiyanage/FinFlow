// Build configuration updated: 2025-04-24 06:50:33 by SakithLiyanage

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Added JitPack repository for MPAndroidChart library
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "FinFlow"
include(":app")