plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
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
            implementation(compose.runtime)
            api(project(":unicompose"))
            api(project(":unicompose-style"))
        }

        val composeAppMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }

        jsMain.dependencies {
            implementation(compose.html.core)
        }
    }
}

android {
    namespace = "dev.unicompose.base"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Apply the unicompose CSS-extraction compiler plugin to compileKotlinJs so
// top-level Style vals defined here (CardStyle, future widget defaults) get
// extracted to the .css file alongside per-app styles. Each module emits its
// own unicompose-generated.css; the consumer's previewSite Copy concatenates
// (or just picks up the most recent — for now the consumer's CSS wins).
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
