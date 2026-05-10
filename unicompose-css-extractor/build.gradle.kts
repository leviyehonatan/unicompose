// Kotlin compiler IR plugin: walks IR for `dev.unicompose.style.Style(...)`
// constructor invocations during compileKotlinJs and emits a static
// `unicompose-generated.css` file. Replaces a runtime AtomicCss injection
// with a build-time-extracted stylesheet.
//
// This module produces TWO things:
//   1. The compiler plugin itself (CompilerPluginRegistrar + IrGenerationExtension)
//   2. A Gradle plugin (KotlinCompilerPluginSupportPlugin) that wires the
//      compiler plugin into consumer kotlin compile tasks.
//
// Both live here for now. They share the same `compileOnly` kotlin-compiler-
// embeddable so the plugin code can reference compiler symbols.
plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
    `maven-publish`
}

// Group + version match what UnicomposeCssExtractorGradlePlugin.getPluginArtifact()
// declares. With `pluginManagement.includeBuild("unicompose-css-extractor")` in
// the root settings, Gradle substitutes any consumer dependency on these
// coordinates with the composite-build's local artifact — no Maven trip.
group = "dev.unicompose"
version = "0.1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.2.20")
}

gradlePlugin {
    plugins {
        create("unicomposeCssExtractor") {
            id = "dev.unicompose.css-extractor"
            implementationClass = "dev.unicompose.extractor.UnicomposeCssExtractorGradlePlugin"
        }
    }
}
