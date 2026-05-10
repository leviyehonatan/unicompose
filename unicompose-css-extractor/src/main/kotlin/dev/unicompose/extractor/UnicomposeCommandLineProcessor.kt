package dev.unicompose.extractor

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

/**
 * Bridges Gradle (or any kotlinc CLI consumer) → the compiler plugin's
 * `CompilerConfiguration`. Receives `-P plugin:dev.unicompose.css-extractor:<opt>=<val>`
 * pairs and stores them on keys the IR extension reads at runtime.
 *
 * Currently exposes:
 *  - `outputDir` — absolute path to the directory the IR extension should
 *    write `unicompose-generated.css` into. Falls back to `/tmp` if unset.
 */
@OptIn(ExperimentalCompilerApi::class)
public class UnicomposeCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "dev.unicompose.css-extractor"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = "outputDir",
            valueDescription = "directory",
            description = "Directory to write unicompose-generated.css into",
            required = false,
            allowMultipleOccurrences = false,
        ),
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            "outputDir" -> configuration.put(KEY_OUTPUT_DIR, value)
        }
    }

    public companion object {
        public val KEY_OUTPUT_DIR: CompilerConfigurationKey<String> =
            CompilerConfigurationKey.create("unicompose css output directory")
    }
}
