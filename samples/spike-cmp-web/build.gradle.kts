// Throwaway spike — confirms CMP-for-Web JS target compiles and produces a
// runnable bundle in our build setup. Single jsMain with no expect/actual
// complexity and no dependency on the unicompose modules. If this works, we
// have the green light to restructure unicompose for a real dual-JS-target
// architecture.

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(17)

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "spike-cmp-web.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}
