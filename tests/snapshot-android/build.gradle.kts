plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.paparazzi)
}

// Paparazzi-based snapshot tests for the Android render path. Runs on the JVM
// (no emulator) using LayoutLib to render Compose. Goldens live under
// src/test/snapshots/images/ and are committed.
//
// Run goldens:    ./gradlew :tests:snapshot-android:verifyPaparazziDebug
// Update goldens: ./gradlew :tests:snapshot-android:recordPaparazziDebug
//
// This is a pure Android library — Paparazzi's plugin doesn't compose well
// with KMP modules, so we keep it isolated here and depend on the published
// modules' android variants.
android {
    namespace = "dev.unicompose.snapshot"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":unicompose"))
    implementation(project(":unicompose-base"))
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    testImplementation("junit:junit:4.13.2")
}
