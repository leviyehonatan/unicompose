package dev.unicompose.extractor

import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable

/**
 * Compile-time evaluator for the subset of IR expressions that can appear as
 * arguments inside a top-level / object-level `Style(...)` constructor call.
 *
 * Returns null when an expression isn't statically resolvable (e.g. token
 * reads from `currentTokens()`, runtime conditionals, function calls outside
 * the known whitelist). Callers treat null as "fall back to the runtime
 * AtomicCss path."
 *
 * The whitelist matches the actual factories used in the unicompose Style API:
 *   - Int.sp / Int.dp / Float.sp / Float.dp                 → Sp(value) / Dp(value)
 *   - Color.Companion.<get-White|Black|Transparent>          → known constants
 *   - rgb(r, g, b) / rgba(r, g, b, a) / argb(a, r, g, b)     → Color
 *   - Padding.Companion.all(dp)                              → uniform Padding
 *   - Padding.Companion.symmetric(vertical, horizontal)      → symmetric Padding
 *   - BorderRadius.Companion.all(dp)                         → uniform BorderRadius
 *   - any enum entry                                         → the entry name
 *   - any IrConst literal                                    → its Kotlin value
 *   - IrGetValue → recursively follow to its initializer
 */
internal class Evaluator {

    /** Compile-time-known value model — kept intentionally small for now. */
    sealed class V {
        data class Bool(val v: Boolean) : V()
        data class Int(val v: kotlin.Int) : V()
        data class Long(val v: kotlin.Long) : V()
        data class Float(val v: kotlin.Float) : V()
        data class Str(val v: String) : V()
        /** Logical Sp value with float-equivalent magnitude. */
        data class Sp(val v: kotlin.Float) : V()
        /** Logical Dp value with float-equivalent magnitude. */
        data class Dp(val v: kotlin.Float) : V()
        /** Color stored as packed ARGB int (0xAARRGGBB). */
        data class Color(val argb: kotlin.Int) : V()
        /** Box-model padding/margin: top, right, bottom, left in Dp. */
        data class Padding(val top: kotlin.Float, val right: kotlin.Float, val bottom: kotlin.Float, val left: kotlin.Float) : V()
        /** Corner radii: top-left, top-right, bottom-right, bottom-left in Dp. */
        data class BorderRadius(val tl: kotlin.Float, val tr: kotlin.Float, val br: kotlin.Float, val bl: kotlin.Float) : V()
        /** A named enum entry (e.g. `FontWeight.Bold`). */
        data class EnumEntry(val enumFqn: String, val name: String) : V()
        /** A named singleton object (e.g. `Size.FillParent`). */
        data class ObjectRef(val fqn: String) : V()
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    fun evaluate(expr: IrExpression?): V? {
        return when (expr) {
            null -> null
            is IrConst -> evaluateConst(expr)
            is IrGetEnumValue -> {
                val owner = expr.symbol.owner
                val enumFqn = (owner.parent as? org.jetbrains.kotlin.ir.declarations.IrClass)
                    ?.fqNameWhenAvailable?.asString() ?: return null
                V.EnumEntry(enumFqn, owner.name.asString())
            }
            is IrGetObjectValue -> {
                val fqn = expr.symbol.owner.fqNameWhenAvailable?.asString() ?: return null
                // For known singletons (e.g. Color.Companion accessed as receiver),
                // we'd return a marker; but a bare object reference like `Size.FillParent`
                // is a typed singleton — emit an ObjectRef the caller can map.
                V.ObjectRef(fqn)
            }
            is IrGetValue -> {
                // Follow temp locals (compiler-generated) and user vals back to their
                // initializer expression. Function parameters have no initializer.
                val owner = expr.symbol.owner as? IrVariable ?: return null
                evaluate(owner.initializer)
            }
            is IrCall -> evaluateCall(expr)
            is IrConstructorCall -> {
                // Constructor calls inside Style args (e.g. `Padding(8.dp, ...)` directly)
                // — handled via the same call path, falls through to evaluateCall logic
                // for the constructed-class FQN match.
                evaluateCtor(expr)
            }
            else -> null
        }
    }

    private fun evaluateConst(c: IrConst): V? = when (val v = c.value) {
        is Boolean -> V.Bool(v)
        is Int -> V.Int(v)
        is Long -> V.Long(v)
        is Float -> V.Float(v)
        is Double -> V.Float(v.toFloat())
        is String -> V.Str(v)
        else -> null
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun evaluateCall(call: IrCall): V? {
        val target = call.symbol.owner.fqNameWhenAvailable?.asString() ?: return null
        val args = call.arguments.map { evaluate(it) }

        return when (target) {
            "dev.unicompose.style.<get-sp>" -> {
                // dispatch receiver = Int or Float; a property getter call on the receiver.
                val receiver = args.firstOrNull() ?: return null
                when (receiver) {
                    is V.Int -> V.Sp(receiver.v.toFloat())
                    is V.Float -> V.Sp(receiver.v)
                    else -> null
                }
            }
            "dev.unicompose.style.<get-dp>" -> {
                val receiver = args.firstOrNull() ?: return null
                when (receiver) {
                    is V.Int -> V.Dp(receiver.v.toFloat())
                    is V.Float -> V.Dp(receiver.v)
                    else -> null
                }
            }
            "dev.unicompose.style.Padding.Companion.all" -> {
                // Companion call: args = [companion-receiver, dp]
                val dp = (args.getOrNull(1) as? V.Dp) ?: return null
                V.Padding(dp.v, dp.v, dp.v, dp.v)
            }
            "dev.unicompose.style.Padding.Companion.symmetric" -> {
                val vertical = (args.getOrNull(1) as? V.Dp) ?: return null
                val horizontal = (args.getOrNull(2) as? V.Dp) ?: return null
                V.Padding(vertical.v, horizontal.v, vertical.v, horizontal.v)
            }
            "dev.unicompose.style.BorderRadius.Companion.all" -> {
                val dp = (args.getOrNull(1) as? V.Dp) ?: return null
                V.BorderRadius(dp.v, dp.v, dp.v, dp.v)
            }
            "dev.unicompose.style.rgb" -> {
                val r = (args.getOrNull(0) as? V.Int)?.v ?: return null
                val g = (args.getOrNull(1) as? V.Int)?.v ?: return null
                val b = (args.getOrNull(2) as? V.Int)?.v ?: return null
                V.Color((0xFF shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF))
            }
            "dev.unicompose.style.argb" -> {
                val a = (args.getOrNull(0) as? V.Int)?.v ?: return null
                val r = (args.getOrNull(1) as? V.Int)?.v ?: return null
                val g = (args.getOrNull(2) as? V.Int)?.v ?: return null
                val b = (args.getOrNull(3) as? V.Int)?.v ?: return null
                V.Color(((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF))
            }
            "dev.unicompose.style.Color.Companion.<get-White>" -> V.Color(0xFFFFFFFF.toInt())
            "dev.unicompose.style.Color.Companion.<get-Black>" -> V.Color(0xFF000000.toInt())
            "dev.unicompose.style.Color.Companion.<get-Transparent>" -> V.Color(0)
            else -> null
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun evaluateCtor(call: IrConstructorCall): V? {
        val classFqn = call.symbol.owner.constructedClass.fqNameWhenAvailable?.asString() ?: return null
        val args = call.arguments.map { evaluate(it) }
        return when (classFqn) {
            "dev.unicompose.style.Color" -> {
                val argb = (args.firstOrNull() as? V.Int)?.v ?: return null
                V.Color(argb)
            }
            else -> null
        }
    }
}
