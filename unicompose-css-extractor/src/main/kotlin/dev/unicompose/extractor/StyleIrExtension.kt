package dev.unicompose.extractor

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

private const val STYLE_FQN = "dev.unicompose.style.Style"

/**
 * IR-level extension that walks every compiled module looking for invocations
 * of the `dev.unicompose.style.Style` constructor and (eventually) emits a
 * static CSS file with the equivalent atomic-CSS rules.
 *
 * Current state: discovers every Style(...) call and dumps a structural
 * breakdown of its arguments to /tmp/unicompose-css-extractor-debug.log so
 * we can see what IR shapes we need to handle as we wire up real extraction.
 */
internal class StyleIrExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val visitor = StyleVisitor()
        moduleFragment.files.forEach { file ->
            visitor.currentFile = file
            file.acceptVoid(visitor)
        }
        if (visitor.discovered.isNotEmpty()) {
            System.err.println(
                "[unicompose-css-extractor] ${moduleFragment.name.asString()}: " +
                    "discovered ${visitor.discovered.size} Style() call sites",
            )
            visitor.discovered.forEach { System.err.println("  $it") }
        }
        if (visitor.dump.isNotEmpty()) {
            runCatching {
                val out = java.io.File("/tmp/unicompose-css-extractor-debug.log")
                out.writeText(
                    "module: ${moduleFragment.name.asString()} (${visitor.discovered.size} sites)\n" +
                        visitor.dump.joinToString("\n"),
                )
            }
        }
    }
}

private class StyleVisitor : IrVisitorVoid() {
    var currentFile: IrFile? = null
    val discovered: MutableList<String> = mutableListOf()
    val dump: MutableList<String> = mutableListOf()

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
            dump += "\nStyle() at ${path.substringAfterLast('/')}:$line"
            dumpArguments(expression, indent = "  ")
        }
        super.visitConstructorCall(expression)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun dumpArguments(call: IrConstructorCall, indent: String) {
        val params = call.symbol.owner.parameters
        val evaluator = Evaluator()
        var allEvaluable = true
        params.forEachIndexed { idx, param ->
            val arg = call.arguments.getOrNull(idx) ?: return@forEachIndexed
            val evaluated = evaluator.evaluate(arg)
            if (evaluated == null) {
                allEvaluable = false
                dump += "$indent${param.name.asString()} = ??? (${describe(arg, "$indent  ")})"
            } else {
                dump += "$indent${param.name.asString()} = $evaluated"
            }
        }
        dump += if (allEvaluable) "$indent[STATIC — extractable]" else "$indent[DYNAMIC — runtime]"
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun describe(expr: IrExpression?, indent: String): String = when (expr) {
        null -> "<null>"
        is IrConst -> "Const(${expr.kind}, ${expr.value})"
        is IrGetEnumValue -> "EnumValue(${expr.symbol.owner.parentClassOrNull()}.${expr.symbol.owner.name.asString()})"
        is IrGetObjectValue -> "ObjectValue(${expr.symbol.owner.fqNameWhenAvailable?.asString() ?: "<no-fqn>"})"
        is IrGetValue -> {
            val owner = expr.symbol.owner
            val name = owner.name.asString()
            // For local vals (compiler-generated temps for named args with defaults,
            // user vals, etc.), follow to the initializer to get the actual value.
            val initializer = (owner as? IrVariable)?.initializer
            if (initializer != null) {
                "GetValue(name=$name) ->\n$indent  ${describe(initializer, "$indent    ")}"
            } else {
                "GetValue(name=$name, owner=${owner::class.simpleName})"
            }
        }
        is IrGetField -> {
            val name = expr.symbol.owner.name.asString()
            "GetField(name=$name, fqn=${expr.symbol.owner.fqNameWhenAvailable?.asString()})"
        }
        is IrCall -> {
            val target = expr.symbol.owner.fqNameWhenAvailable?.asString() ?: "<no-fqn>"
            val recv = expr.arguments.firstOrNull()?.let { "\n${indent}dispatch=${describe(it, "$indent  ")}" } ?: ""
            val args = expr.arguments.drop(1).joinToString("") { a ->
                "\n${indent}arg=${describe(a, "$indent  ")}"
            }
            "Call($target)$recv$args"
        }
        is IrConstructorCall -> {
            val target = expr.symbol.owner.constructedClass.fqNameWhenAvailable?.asString() ?: "<no-fqn>"
            "ConstructorCall($target)"
        }
        else -> "${expr::class.simpleName}"
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun org.jetbrains.kotlin.ir.declarations.IrEnumEntry.parentClassOrNull(): String =
        (this.parent as? org.jetbrains.kotlin.ir.declarations.IrClass)?.fqNameWhenAvailable?.asString() ?: "<unknown>"
}
