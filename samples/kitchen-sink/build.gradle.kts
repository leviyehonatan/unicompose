plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // js(IR) — DOM bundle, real <span>/<div>/<button> via Compose HTML.
    // wasmJs — Skia canvas bundle, mobile-equivalent rendering via CMP-for-Web.
    // Both produce executable browser bundles; they coexist for production+preview
    // and for visual A/B testing (canvas output ≈ what mobile actually renders).
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

// `./gradlew :samples:kitchen-sink:previewSite` produces a single dist/preview/
// directory containing both bundles plus a side-by-side compare.html. Serve with
// `python3 -m http.server` from build/dist/preview/ to view both at once or to
// drive Playwright A/B comparisons against /html/ and /canvas/ subpaths.
val previewSite by tasks.registering(Copy::class) {
    dependsOn("jsBrowserDistribution", "wasmJsBrowserDistribution")
    from(layout.buildDirectory.dir("dist/js/productionExecutable")) { into("html") }
    from(layout.buildDirectory.dir("dist/wasmJs/productionExecutable")) { into("canvas") }
    into(layout.buildDirectory.dir("dist/preview"))
    doLast {
        layout.buildDirectory.file("dist/preview/compare.html").get().asFile.writeText(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <title>unicompose — DOM vs Canvas comparison</title>
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
                    <h1>unicompose — same App() rendered two ways
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
