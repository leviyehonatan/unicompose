package dev.unicompose.extractor

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

private const val STYLE_FQN = "dev.unicompose.style.Style"

/**
 * IR-level extension that walks every compiled module looking for invocations
 * of the `dev.unicompose.style.Style` constructor. For each call site found,
 * logs the file + line for now (CSS extraction follows in a subsequent task).
 *
 * Runs against IR rather than KSP-style declarations because Style() is most
 * commonly invoked inline inside @Composable function bodies — which KSP does
 * not expose. IR sees every construction regardless of where it appears.
 */
internal class StyleIrExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val visitor = StyleVisitor()
        moduleFragment.files.forEach { file ->
            visitor.currentFile = file
            file.acceptVoid(visitor)
        }
        if (visitor.discovered.isNotEmpty()) {
            // Stderr is the conventional channel for compiler-plugin diagnostics;
            // Gradle's Kotlin compile task surfaces stderr in the build log.
            System.err.println(
                "[unicompose-css-extractor] ${moduleFragment.name.asString()}: " +
                    "discovered ${visitor.discovered.size} Style() call sites",
            )
            visitor.discovered.forEach { System.err.println("  $it") }
        }
    }
}

private class StyleVisitor : IrVisitorVoid() {
    var currentFile: IrFile? = null
    val discovered: MutableList<String> = mutableListOf()

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitConstructorCall(expression: IrConstructorCall) {
        val classFqn = expression.symbol.owner.constructedClass.fqNameWhenAvailable?.asString()
        if (classFqn == STYLE_FQN) {
            val file = currentFile
            val path = file?.fileEntry?.name ?: "<unknown>"
            val line = file?.fileEntry?.getLineNumber(expression.startOffset)?.let { it + 1 } ?: 0
            discovered += "$path:$line"
        }
        super.visitConstructorCall(expression)
    }
}
