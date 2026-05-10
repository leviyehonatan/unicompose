@file:Suppress("UnstableApiUsage")

pluginManagement {
    // Pull the unicompose-css-extractor Gradle plugin in as a composite build.
    // Consumers can then apply it via plugins { id("dev.unicompose.css-extractor") }.
    // Its own settings.gradle.kts brings the same versions catalog as this
    // root build, so plugin and library versions stay in lockstep.
    includeBuild("unicompose-css-extractor")

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

// Compose the css-extractor build into this build for both plugin resolution
// (via pluginManagement above) AND dependency substitution (this top-level
// includeBuild). The latter is what makes the Kotlin compiler-plugin classpath
// substitute the SubpluginArtifact("dev.unicompose:unicompose-css-extractor:...")
// against the local jar — without it Gradle would try maven central.
includeBuild("unicompose-css-extractor")

include(
    ":unicompose-style",
    ":unicompose",
    ":unicompose-base",
    ":samples:kitchen-sink",
    ":samples:todo-app",
    ":tests:snapshot-android",
    // :unicompose-css-extractor is a composite build (see pluginManagement.
    // includeBuild above), not a subproject of this build.
)
