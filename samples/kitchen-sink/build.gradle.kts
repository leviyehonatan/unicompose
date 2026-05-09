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
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "kitchen-sink.js"
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
            }
            group("web") {
                withJs()
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
