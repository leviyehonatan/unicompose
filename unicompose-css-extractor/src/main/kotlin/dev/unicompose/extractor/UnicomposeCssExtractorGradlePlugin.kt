package dev.unicompose.extractor

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Gradle plugin that wires the unicompose CSS-extraction compiler plugin into
 * a consumer module's `compileKotlinJs` task.
 *
 * Apply via:
 *   plugins { id("dev.unicompose.css-extractor") }
 *
 * What it does:
 * - Identifies itself by id `dev.unicompose.css-extractor` so [getCompilerPluginId]
 *   matches what the [UnicomposeCommandLineProcessor] dispatches `outputDir` against.
 * - On every JS-target main compilation, returns a [SubpluginOption] pointing at
 *   `<consumer>/build/generated/css/` so the IR plugin writes its
 *   `unicompose-generated.css` output there.
 * - Adds the matching jar (this module's published artifact) to the consumer's
 *   Kotlin compiler plugin classpath via [getPluginArtifact]. In a composite-build
 *   setup the local jar is substituted automatically; in a published setup the
 *   coordinates point at Maven.
 *
 * Restricts to the JS target only — wasmJs uses Compose canvas (Skia), not the
 * AtomicCss DOM path, so build-time CSS extraction wouldn't apply there.
 */
@Suppress("unused") // Gradle entry point.
public class UnicomposeCssExtractorGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        // Register a task that extracts the static unicompose-reset.css resource
        // from this plugin's jar into a project-local build dir. Lives in its
        // OWN dir (build/generated/css-reset/) — separate from the IR plugin's
        // output dir, because compileKotlinJs declares the IR-output dir as a
        // task output and Gradle wipes it before each run, which would erase
        // the reset.css if it shared the directory.
        val resetOutputDir = target.layout.buildDirectory.dir("generated/css-reset")
        val extractResetTask = target.tasks.register("extractUnicomposeReset") { task ->
            task.outputs.file(resetOutputDir.map { it.file("unicompose-reset.css") })
            task.doLast {
                val out = resetOutputDir.get().asFile.also { it.mkdirs() }
                val resourceStream = UnicomposeCssExtractorGradlePlugin::class.java
                    .classLoader
                    .getResourceAsStream("unicompose-reset.css")
                    ?: error("unicompose-reset.css not found on css-extractor plugin classpath")
                resourceStream.use { input ->
                    java.io.File(out, "unicompose-reset.css").outputStream().use { input.copyTo(it) }
                }
            }
        }

        // Auto-wire reset.css into the JS distribution: hook the css-reset dir
        // into the jsProcessResources Copy task so unicompose-reset.css ends up
        // bundled next to the JS in the final dist. Consumers don't have to
        // pull it manually from the plugin jar.
        //
        // The Kotlin Multiplatform plugin registers jsProcessResources lazily
        // when the js() target is configured, so we use plugins.withId + a
        // task-name match that fires whenever it appears.
        //
        // Cross-module aggregation of unicompose-generated.css is intentionally
        // NOT auto-wired: a Gradle variants + attributes implementation needs
        // deeper integration with the KMP attribute schema than what's worth
        // it today (a simple `withVariantReselection()` artifactView ends up
        // matching unrelated JVM jars from Compose deps because Gradle's
        // attribute-compatibility rules are loose with novel attributes).
        // Consumers do the cross-module merge in their previewSite Copy task
        // by enumerating which dep modules they want CSS from.
        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            target.tasks.matching { it.name == "jsProcessResources" }.configureEach { task ->
                if (task is org.gradle.api.tasks.Copy) {
                    task.from(resetOutputDir)
                    task.dependsOn(extractResetTask)
                }
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        // platformType.name is "js" for Kotlin/JS targets; "wasm" / "common" / "jvm"
        // / "androidJvm" / "native" for the others. We only emit CSS on the DOM
        // backend, which is the JS target.
        if (kotlinCompilation.platformType.name != "js") return false
        // Only the main compilation — skip JS test compilations.
        return kotlinCompilation.name == "main"
    }

    override fun getCompilerPluginId(): String = "dev.unicompose.css-extractor"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "dev.unicompose",
        artifactId = "unicompose-css-extractor",
        version = "0.1.0-SNAPSHOT",
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val outputDirProvider = project.layout.buildDirectory.dir("generated/css")

        // Register the directory as an output of the compile task so Gradle's
        // up-to-date checks can see the generated CSS file. Also create the
        // dir if it doesn't exist before the task runs (the Kotlin compiler
        // won't create parent dirs for us).
        kotlinCompilation.compileTaskProvider.configure { task ->
            task.outputs.dir(outputDirProvider)
            task.doFirst { outputDirProvider.get().asFile.mkdirs() }
            // Make sure the static reset.css is extracted alongside the
            // generated one. Consumers see both in build/generated/css/.
            task.dependsOn(project.tasks.named("extractUnicomposeReset"))
        }

        return project.provider {
            listOf(
                SubpluginOption(
                    key = "outputDir",
                    value = outputDirProvider.get().asFile.absolutePath,
                ),
            )
        }
    }
}
