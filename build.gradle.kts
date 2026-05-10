plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
}

// `./gradlew visualPreview` assembles every sample's preview bundles into one
// served directory. The Playwright suite points at this dir, so a single
// http-server invocation serves all sample bundles at predictable subpaths.
//
// Layout under build/tests-preview/:
//   /html               — kitchen-sink DOM bundle
//   /canvas             — kitchen-sink canvas bundle
//   /compare.html       — kitchen-sink side-by-side
//   /todo-html          — todo-app DOM bundle
//   /todo-canvas        — todo-app canvas bundle
//   /todo-compare.html  — todo-app side-by-side
val visualPreview by tasks.registering(Copy::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doFirst { delete(layout.buildDirectory.dir("tests-preview")) }
    dependsOn(":samples:kitchen-sink:previewSite", ":samples:todo-app:previewSite")

    val ksPreview = project(":samples:kitchen-sink").layout.buildDirectory.dir("dist/preview")
    val todoPreview = project(":samples:todo-app").layout.buildDirectory.dir("dist/preview")

    // kitchen-sink at top level (legacy paths so existing tests keep working).
    from(ksPreview)
    // todo-app under prefixed paths to avoid collision with kitchen-sink dirs.
    from(todoPreview.map { it.dir("html") }) { into("todo-html") }
    from(todoPreview.map { it.dir("canvas") }) { into("todo-canvas") }
    from(todoPreview.map { it.file("compare.html") }) { rename { "todo-compare.html" } }

    into(layout.buildDirectory.dir("tests-preview"))
}
