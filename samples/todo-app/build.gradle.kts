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

// The css-extractor Gradle plugin auto-wires both unicompose-generated.css
// (cross-module aggregated via the unicomposeCssClasspath resolvable
// configuration) AND unicompose-reset.css into jsProcessResources. They
// ride into /html/ via the existing dist Copy below — no extra wiring
// needed.

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
                // compose.ui for the platform window/host APIs:
                //   - ComposeUIViewController on iOS (iosMain/MainViewController.kt)
                //   - CanvasBasedWindow on wasmJs (wasmJsMain/main.kt)
                // unicompose-style/unicompose pull in compose.foundation
                // transitively for layout primitives, so we don't need it here.
                // material3 was removed when the mechanism layer dropped it.
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

// `./gradlew :samples:todo-app:previewSite` produces both bundles under
// build/dist/preview/. Same shape as kitchen-sink — see samples/kitchen-sink/
// build.gradle.kts for why we use the wasmJs *development* distribution
// (production wasm-opt strips Kotlin/Wasm init code).
val previewSite by tasks.registering(Copy::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doFirst { delete(layout.buildDirectory.dir("dist/preview")) }
    dependsOn("jsBrowserDistribution", "wasmJsBrowserDevelopmentExecutableDistribution")
    from(layout.buildDirectory.dir("dist/js/productionExecutable")) { into("html") }
    from(layout.buildDirectory.dir("dist/wasmJs/developmentExecutable")) { into("canvas") }
    into(layout.buildDirectory.dir("dist/preview"))
}
