// Mirrors samples/kitchen-sink: same target set (Android, iOS x3, JS DOM,
// wasmJs canvas), same hierarchy (composeApp group includes wasmJs), same
// previewSite shape. Only difference is the source code being demoed.

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
    // Build-time CSS extraction for the JS target. Gradle plugin lives in the
    // composite-build at unicompose-css-extractor/; it wires its compiler
    // plugin into compileKotlinJs and points it at <project>/build/generated/css.
    id("dev.unicompose.css-extractor")
}

// Directories the css-extractor produces:
//  - generated/css/ — the IR plugin writes unicompose-generated.css here
//  - generated/css-reset/ — the Gradle plugin extracts the static reset here
val cssExtractorOutputDir = layout.buildDirectory.dir("generated/css")
val cssExtractorResetDir = layout.buildDirectory.dir("generated/css-reset")

kotlin {
    jvmToolchain(17)

    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "todo-app-html.js"
            }
        }
        binaries.executable()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "todoApp"
        browser {
            commonWebpackConfig {
                outputFileName = "todo-app-canvas.js"
            }
        }
        binaries.executable()
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "TodoApp"
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

android {
    namespace = "dev.unicompose.todo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "dev.unicompose.todo"
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

// `./gradlew :samples:todo-app:previewSite` produces both bundles + a
// compare.html under build/dist/preview/. Same shape as kitchen-sink — see
// samples/kitchen-sink/build.gradle.kts for why we use the wasmJs *development*
// distribution (production wasm-opt strips Kotlin/Wasm init code).
val previewSite by tasks.registering(Copy::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doFirst { delete(layout.buildDirectory.dir("dist/preview")) }
    dependsOn("jsBrowserDistribution", "wasmJsBrowserDevelopmentExecutableDistribution")
    from(layout.buildDirectory.dir("dist/js/productionExecutable")) { into("html") }
    from(layout.buildDirectory.dir("dist/wasmJs/developmentExecutable")) { into("canvas") }
    // Pull in the build-time-extracted CSS files. Each module emits its own
    // unicompose-generated.css; we concatenate them into a single file under
    // /html/ so the runtime <link> picks up rules from this app *and* every
    // base-library widget that's been refactored to top-level vals.
    // generated.css per module + the once-per-app reset.css.
    from(cssExtractorOutputDir) { into("html-extras-app") }
    from(project(":unicompose-base").layout.buildDirectory.dir("generated/css")) { into("html-extras-base") }
    from(cssExtractorResetDir) { into("html") }
    into(layout.buildDirectory.dir("dist/preview"))
    dependsOn(":unicompose-base:compileKotlinJs", "extractUnicomposeReset")
    doLast {
        val htmlDir = layout.buildDirectory.file("dist/preview/html").get().asFile
        // Concatenate per-module unicompose-generated.css into one served file.
        val merged = listOf("html-extras-base", "html-extras-app").map { sub ->
            layout.buildDirectory.file("dist/preview/$sub/unicompose-generated.css").get().asFile
        }.filter { it.exists() }.joinToString("\n") { it.readText() }
        if (merged.isNotEmpty()) {
            File(htmlDir, "unicompose-generated.css").writeText(merged)
        }
        // unicompose-reset.css is copied directly into /html/ via the from(...)
        // block above (see cssExtractorResetDir). Nothing to do here.
        // clean up the staging dirs
        listOf("html-extras-app", "html-extras-base").forEach {
            layout.buildDirectory.file("dist/preview/$it").get().asFile.deleteRecursively()
        }
    }
    doLast {
        layout.buildDirectory.file("dist/preview/compare.html").get().asFile.writeText(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <title>todo-app — DOM vs Canvas comparison</title>
                <style>
                    body { margin: 0; font-family: -apple-system, system-ui, sans-serif; }
                    header { padding: 12px 20px; background: #1B1C1F; color: #ECEDEF; }
                    h1 { margin: 0; font-size: 16px; font-weight: 600; }
                    h1 small { font-weight: 400; opacity: 0.7; margin-left: 8px; }
                    .grid { display: grid; grid-template-columns: 1fr 1fr; height: calc(100vh - 88px); gap: 1px; background: #d0d4dc; }
                    .pane { display: flex; flex-direction: column; background: white; }
                    .pane h2 { margin: 0; padding: 8px 16px; font-size: 13px; font-weight: 500; background: #f7f7f8; border-bottom: 1px solid #e6e7eb; }
                    .pane iframe { flex: 1; border: 0; width: 100%; }
                </style>
            </head>
            <body>
                <header>
                    <h1>unicompose todo-app — same App() rendered two ways
                        <small>left: real DOM (production web) · right: Skia canvas (mobile preview)</small>
                    </h1>
                </header>
                <div class="grid">
                    <div class="pane">
                        <h2>HTML / DOM (Compose HTML)</h2>
                        <iframe src="html/index.html"></iframe>
                    </div>
                    <div class="pane">
                        <h2>Canvas / Skia (CMP for Web — what mobile renders)</h2>
                        <iframe src="canvas/index.html"></iframe>
                    </div>
                </div>
            </body>
            </html>
            """.trimIndent()
        )
    }
}
