// Mirrors samples/kitchen-sink: same target set (Android, iOS x3, JS DOM,
// wasmJs canvas), same hierarchy (composeApp group includes wasmJs), same
// previewSite shape. Only difference is the source code being demoed.

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
}

// Apply the unicompose CSS-extraction compiler plugin to the JS target only.
// Direct classpath injection + a CLI option pointing at the build dir for the
// generated CSS. We'll move to plugins { id("dev.unicompose.css-extractor") }
// once the companion Gradle plugin can be applied via a published or
// composite-included artifact.
configurations.matching { it.name == "kotlinCompilerPluginClasspathJsMain" }.configureEach {
    dependencies.add(project.dependencies.create(project(":unicompose-css-extractor")))
}

val cssExtractorOutputDir = layout.buildDirectory.dir("generated/css")
tasks.matching { it.name == "compileKotlinJs" }.configureEach {
    val outDir = cssExtractorOutputDir.get().asFile
    outputs.dir(outDir)
    doFirst { outDir.mkdirs() }
    (this as org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>)
        .compilerOptions.freeCompilerArgs.addAll(
            "-P",
            "plugin:dev.unicompose.css-extractor:outputDir=${outDir.absolutePath}",
        )
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
    // Pull in the build-time-extracted CSS file so the served HTML can <link>
    // against /html/unicompose-generated.css. The compileKotlinJs configuration
    // above ensures it exists before this Copy runs.
    from(cssExtractorOutputDir) { into("html") }
    into(layout.buildDirectory.dir("dist/preview"))
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
