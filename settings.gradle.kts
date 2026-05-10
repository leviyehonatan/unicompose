@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // Kotlin/JS plugin downloads Node from here.
        ivy("https://nodejs.org/dist/") {
            name = "nodejs"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        // Kotlin/JS plugin downloads Yarn from here.
        ivy("https://github.com/yarnpkg/yarn/releases/download/") {
            name = "yarn"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        // Kotlin/Wasm plugin downloads Binaryen (the wasm-opt toolchain) from here.
        ivy("https://github.com/WebAssembly/binaryen/releases/download/") {
            name = "binaryen"
            patternLayout { artifact("version_[revision]/[artifact]-version_[revision]-[classifier].[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

rootProject.name = "unicompose-root"

include(
    ":unicompose-style",
    ":unicompose",
    ":unicompose-base",
    ":samples:kitchen-sink",
    ":samples:todo-app",
    ":tests:snapshot-android",
    ":unicompose-css-extractor",
)
