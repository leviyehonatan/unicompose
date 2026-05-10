// Composite build root for the unicompose-css-extractor Kotlin compiler
// + Gradle plugin. Lives in its own Gradle build so the parent monorepo
// can include it via `pluginManagement.includeBuild(...)` and consumers
// can apply it as a normal `plugins { id("dev.unicompose.css-extractor") }`
// block — no buildscript classpath poking, no internal-API workarounds.
//
// The parent build's settings.gradle.kts references this directory; this
// file just bootstraps Gradle for the included build.

@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
    }

    // Reuse the parent's libs.versions.toml so plugin/library versions stay
    // in lockstep with the rest of the monorepo. The relative path resolves
    // to <repo-root>/gradle/libs.versions.toml.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "unicompose-css-extractor"
