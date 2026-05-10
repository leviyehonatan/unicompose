package dev.unicompose.extractor

import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Custom value for the standard [Category] attribute. Other variants in the
 * dep graph (regular library jars, sources, javadoc, etc.) use the standard
 * `Category.LIBRARY` / `Category.DOCUMENTATION` values, so this value being
 * unique means our resolvable configuration ONLY matches our own producer
 * configurations. No need for `withVariantReselection()` or lenient mode.
 */
internal const val UNICOMPOSE_CSS_CATEGORY: String = "unicompose-extracted-css"

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

        // Cross-module CSS aggregation via Gradle "variants and attributes":
        // each module that applies this plugin both PRODUCES its own
        // unicompose-generated.css and CONSUMES the same artifact from every
        // module on its compile graph.
        //
        // The trick that makes this clean: tag both producer + consumer with
        // a custom `Category` value (a standard Gradle attribute). Other
        // variants in the dep graph carry `Category=library` (regular jars),
        // `Category=documentation` (sources / javadoc), etc., so the
        // attribute matcher cleanly filters them out. We don't need
        // `withVariantReselection()` or `isLenient = true` because the
        // Category attribute is strict by default — variants without our
        // category value simply don't match.
        val cssCategory = target.objects.named(Category::class.java, UNICOMPOSE_CSS_CATEGORY)

        val generatedCssDir = target.layout.buildDirectory.dir("generated/css")
        val generatedCssFile = generatedCssDir.map { it.file("unicompose-generated.css") }

        // Producer: exposes this module's own unicompose-generated.css.
        val producerConfig = target.configurations.create("unicomposeCssElements") { config ->
            config.isCanBeConsumed = true
            config.isCanBeResolved = false
            config.attributes.attribute(Category.CATEGORY_ATTRIBUTE, cssCategory)
        }
        target.artifacts.add(producerConfig.name, generatedCssFile) { artifact ->
            artifact.builtBy("compileKotlinJs")
        }

        // Consumer: extends from the JS+common dep configurations so the dep
        // graph mirrors what compileKotlinJs sees. Only deps that ALSO apply
        // our plugin (and therefore expose the CATEGORY=unicompose-extracted-
        // css variant) contribute artifacts.
        val consumerConfig = target.configurations.create("unicomposeCssClasspath") { config ->
            config.isCanBeConsumed = false
            config.isCanBeResolved = true
            // Set the Category attribute on the configuration itself so
            // Gradle's variant resolver knows what we want before traversing
            // the dep graph.
            config.attributes.attribute(Category.CATEGORY_ATTRIBUTE, cssCategory)
            target.afterEvaluate {
                listOf("jsMainImplementation", "jsMainApi", "commonMainImplementation", "commonMainApi")
                    .mapNotNull { target.configurations.findByName(it) }
                    .forEach { config.extendsFrom(it) }
            }
        }
        // Lenient view to silently drop deps that don't expose our variant.
        val resolvedCssFiles = consumerConfig.incoming.artifactView { view ->
            view.isLenient = true
        }.files

        // Auto-wire BOTH the static reset.css AND the cross-module aggregated
        // generated.css into the JS distribution via jsProcessResources.
        // Consumers get both files in their dist with zero extra wiring.
        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            target.tasks.matching { it.name == "jsProcessResources" }.configureEach { task ->
                if (task is org.gradle.api.tasks.Copy) {
                    task.from(resetOutputDir)
                    task.dependsOn(extractResetTask)

                    // Aggregate every dep's generated.css + this module's own
                    // into ONE merged file. The merge runs at task execution
                    // time so the per-module compileKotlinJs runs first.
                    val mergedFileProvider = target.layout.buildDirectory
                        .file("generated/css-merged/unicompose-generated.css")
                    task.inputs.files(resolvedCssFiles, generatedCssFile)
                    task.dependsOn("compileKotlinJs")
                    task.from(mergedFileProvider) { spec ->
                        spec.duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
                    }
                    task.doFirst {
                        val depCss = resolvedCssFiles
                            .filter { it.exists() }
                            .joinToString("\n") { it.readText() }
                        val ownCss = generatedCssFile.get().asFile
                            .takeIf { it.exists() }?.readText().orEmpty()
                        val merged = listOf(depCss, ownCss).filter { it.isNotEmpty() }.joinToString("\n")
                        val out = mergedFileProvider.get().asFile
                        out.parentFile.mkdirs()
                        out.writeText(merged)
                    }
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
