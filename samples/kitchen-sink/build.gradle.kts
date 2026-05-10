plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
    id("dev.unicompose.css-extractor")
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "kitchen-sink-html.js"
            }
        }
        binaries.executable()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        // outputModuleName matches the canonical kotlin-wasm-compose-template;
        // without it, webpack's bundle wrapping doesn't auto-invoke main()
        // because the module name doesn't align with the entry chunk.
        outputModuleName = "kitchenSink"
        browser {
            commonWebpackConfig {
                outputFileName = "kitchen-sink-canvas.js"
            }
        }
        binaries.executable()
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "KitchenSink"
            isStatic = true
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("composeApp") {
                withAndroidTarget()
                group("ios") {
                    withIosX64()
                    withIosArm64()
                    withIosSimulatorArm64()
                }
                withWasmJs()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":unicompose"))
            implementation(project(":unicompose-base"))
            implementation(compose.runtime)
        }

        val composeAppMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
        }
    }
}

// CSS extractor auto-wires generated.css + reset.css into jsProcessResources.

// `./gradlew :samples:kitchen-sink:previewSite` produces a single dist/preview/
// directory containing both bundles plus a side-by-side compare.html. Serve with
// `python3 -m http.server` from build/dist/preview/ to view both at once or to
// drive Playwright A/B comparisons against /html/ and /canvas/ subpaths.
//
// `./gradlew :samples:kitchen-sink:visualTest` runs Playwright golden tests
// against both bundles. See tests/visual/README.md for details.

private val visualTestDir = rootProject.layout.projectDirectory.dir("tests/visual").asFile

// Ensures node_modules/ is populated. Idempotent; npm skips when up-to-date.
val visualTestNpmInstall by tasks.registering(Exec::class) {
    workingDir = visualTestDir
    commandLine = listOf("npm", "install", "--no-audit", "--no-fund")
    inputs.file(File(visualTestDir, "package.json"))
    outputs.dir(File(visualTestDir, "node_modules"))
}

// Downloads the Chromium binary Playwright drives. Cached per-machine in
// ~/.cache/ms-playwright; the task is idempotent after first run.
val visualTestInstallBrowsers by tasks.registering(Exec::class) {
    dependsOn(visualTestNpmInstall)
    workingDir = visualTestDir
    commandLine = listOf("npx", "playwright", "install", "chromium")
}

val visualTest by tasks.registering(Exec::class) {
    group = "verification"
    description = "Run Playwright visual regression tests against the html + canvas bundles."
    dependsOn(visualTestInstallBrowsers, "previewSite")
    workingDir = visualTestDir
    commandLine = listOf("npx", "playwright", "test")
}

val visualTestUpdate by tasks.registering(Exec::class) {
    group = "verification"
    description = "Regenerate Playwright golden screenshots."
    dependsOn(visualTestInstallBrowsers, "previewSite")
    workingDir = visualTestDir
    commandLine = listOf("npx", "playwright", "test", "--update-snapshots")
}
val previewSite by tasks.registering(Copy::class) {
    // Wipe stale artifacts from prior builds (Copy doesn't delete by default,
    // and old hashed-name wasm files would otherwise accumulate).
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doFirst {
        delete(layout.buildDirectory.dir("dist/preview"))
    }
    dependsOn("jsBrowserDistribution", "wasmJsBrowserDevelopmentExecutableDistribution")
    from(layout.buildDirectory.dir("dist/js/productionExecutable")) { into("html") }
    // Use the wasmJs *development* distribution, not the production one.
    // Compose Multiplatform 1.10's production pipeline runs Binaryen wasm-opt,
    // which over-aggressively strips init code from the Kotlin/Wasm output
    // and leaves a bundle that loads but never invokes main(). Dev distribution
    // skips wasm-opt and renders correctly. Bundle is ~10× larger (19 MB vs
    // 1.5 MB for the app wasm) but that's acceptable for a dev/preview/test
    // artifact. Production-mode rendering on the web is the *DOM* bundle's job.
    from(layout.buildDirectory.dir("dist/wasmJs/developmentExecutable")) { into("canvas") }
    into(layout.buildDirectory.dir("dist/preview"))
}

android {
    namespace = "dev.unicompose.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "dev.unicompose.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.0.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
