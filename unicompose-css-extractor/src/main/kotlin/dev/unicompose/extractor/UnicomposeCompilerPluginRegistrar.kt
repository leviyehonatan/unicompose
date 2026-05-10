package dev.unicompose.extractor

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Entry point for the unicompose CSS-extraction compiler plugin.
 *
 * Wires the IR-level extension that walks every `dev.unicompose.style.Style(...)`
 * constructor call in compiled code and (eventually) emits a static CSS file
 * with the equivalent atomic-CSS rules.
 *
 * Discovered via `META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar`.
 *
 * Supports K2 (Kotlin 2.0+).
 */
@OptIn(ExperimentalCompilerApi::class)
public class UnicomposeCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(StyleIrExtension())
    }
}
