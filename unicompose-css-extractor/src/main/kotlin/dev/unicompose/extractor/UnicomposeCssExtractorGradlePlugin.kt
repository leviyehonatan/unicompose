package dev.unicompose.extractor

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Gradle plugin that wires the unicompose CSS-extraction compiler plugin into
 * a consumer module's Kotlin compile tasks.
 *
 * Apply via:
 *   plugins { id("dev.unicompose.css-extractor") }
 *
 * Currently activates on every Kotlin compilation; we'll narrow to JS/wasmJs
 * once the extractor's output is wired into the bundle.
 *
 * The plugin artifact (this module) needs to be on the consumer's
 * `kotlinCompilerPluginClasspath` configuration. KotlinCompilerPluginSupportPlugin
 * handles that automatically once `getPluginArtifact()` returns the right
 * coordinates. For local development inside this multi-module project we
 * substitute via the `kotlinCompilerPluginClasspath*` configuration directly.
 */
@Suppress("unused") // Gradle entry point.
public class UnicomposeCssExtractorGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        // Default impl — the meaningful work is done in applyToCompilation /
        // getPluginArtifact below.
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = "dev.unicompose.css-extractor"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "dev.unicompose",
        artifactId = "unicompose-css-extractor",
        version = "0.1.0-SNAPSHOT",
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }
}
