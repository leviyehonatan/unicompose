package dev.unicompose.extractor

/**
 * Turns a map of statically-evaluated Style argument values into a list of
 * CSS `(property, value)` rules + a deterministic class name hash.
 *
 * Mirrors the format produced by `unicompose/src/jsMain/.../style/AtomicCss.kt`'s
 * `visualRules()` and `stableHash()` so build-time-emitted classes resolve to the
 * SAME hash that the runtime path would have produced for the same Style — the
 * runtime path stays as a fallback for non-extractable styles, and identical
 * hashes mean both paths produce the same CSS rule set.
 *
 * Returns null when no known Style fields could be mapped (e.g. only opaque
 * dynamic args were present). Otherwise: a class name like "uc-abc1234" plus
 * the body rules to write into `.<class>{<body>}`.
 */
internal object CssEmitter {

    data class Emission(val className: String, val cssBody: String)

    /**
     * Maps an evaluated Style param-name → V to a list of CSS rules. Param names
     * not in the whitelist are silently skipped (we'd rather emit a partial class
     * for the known fields than fail the whole call).
     */
    fun emit(args: Map<String, Evaluator.V>, prefix: String = "uc"): Emission? {
        val rules = mutableListOf<Pair<String, String>>()

        // Process in a stable order so the same Style produces the same hash
        // regardless of map iteration order.
        for (paramName in ParamOrder) {
            val v = args[paramName] ?: continue
            ruleFor(paramName, v)?.let { rules += it }
        }

        if (rules.isEmpty()) return null
        val body = rules.joinToString(";") { "${it.first}:${it.second}" }
        val key = "$prefix|$body"
        val className = "$prefix-${stableHash(key)}"
        return Emission(className, body)
    }

    /**
     * Stable order matching the order AtomicCss.visualRules() emits — keeps the
     * hash key text identical across the two paths.
     *
     * NOTE: ref params (e.g. backgroundColorRef) substitute their literal
     * counterparts when present. They occupy the literal's position in the
     * emit order so a Style switching between literal and ref produces a
     * different hash (correct — different rule).
     */
    private val ParamOrder = listOf(
        "padding", "margin",
        "backgroundColor", "backgroundColorRef",
        "color", "colorRef",
        "fontSize", "fontWeight",
        "fontFamily", "lineHeight", "letterSpacing", "textAlign", "borderRadius",
        "border", "boxShadow", "opacity", "width", "height", "flex",
        "gap", "gapRef",
    )

    private fun ruleFor(paramName: String, v: Evaluator.V): Pair<String, String>? = when (paramName) {
        "padding" -> (v as? Evaluator.V.Padding)?.let {
            "padding" to "${it.top.fmt()}px ${it.right.fmt()}px ${it.bottom.fmt()}px ${it.left.fmt()}px"
        }
        "margin" -> (v as? Evaluator.V.Padding)?.let {
            "margin" to "${it.top.fmt()}px ${it.right.fmt()}px ${it.bottom.fmt()}px ${it.left.fmt()}px"
        }
        "backgroundColor" -> (v as? Evaluator.V.Color)?.let { "background-color" to colorCss(it.argb) }
        "backgroundColorRef" -> (v as? Evaluator.V.Str)?.let { "background-color" to "var(${it.v})" }
        "color" -> (v as? Evaluator.V.Color)?.let { "color" to colorCss(it.argb) }
        "colorRef" -> (v as? Evaluator.V.Str)?.let { "color" to "var(${it.v})" }
        "gap" -> (v as? Evaluator.V.Dp)?.let { "gap" to "${it.v.fmt()}px" }
        "gapRef" -> (v as? Evaluator.V.Str)?.let { "gap" to "var(${it.v})" }
        "fontSize" -> (v as? Evaluator.V.Sp)?.let { "font-size" to "${it.v.fmt()}px" }
        "fontWeight" -> (v as? Evaluator.V.EnumEntry)?.let { fontWeightCss(it.name) }
        "fontFamily" -> (v as? Evaluator.V.EnumEntry)?.let { fontFamilyCss(it.name) }
        "lineHeight" -> (v as? Evaluator.V.Sp)?.let { "line-height" to "${it.v.fmt()}px" }
        "letterSpacing" -> (v as? Evaluator.V.Sp)?.let { "letter-spacing" to "${it.v.fmt()}px" }
        "textAlign" -> (v as? Evaluator.V.EnumEntry)?.let { textAlignCss(it.name) }
        "borderRadius" -> (v as? Evaluator.V.BorderRadius)?.let {
            "border-radius" to "${it.tl.fmt()}px ${it.tr.fmt()}px ${it.br.fmt()}px ${it.bl.fmt()}px"
        }
        "opacity" -> when (v) {
            is Evaluator.V.Float -> "opacity" to v.v.toString()
            is Evaluator.V.Int -> "opacity" to v.v.toString()
            else -> null
        }
        "width" -> sizeCss(v)?.let { "width" to it }
        "height" -> sizeCss(v)?.let { "height" to it }
        "flex" -> when (v) {
            is Evaluator.V.Float -> "flex" to "${v.v} 1 0%"
            is Evaluator.V.Int -> "flex" to "${v.v} 1 0%"
            else -> null
        }
        else -> null
    }

    private fun colorCss(argb: Int): String {
        val a = (argb ushr 24) and 0xFF
        val r = (argb ushr 16) and 0xFF
        val g = (argb ushr 8) and 0xFF
        val b = argb and 0xFF
        return if (a == 0xFF) "rgb($r,$g,$b)" else "rgba($r,$g,$b,${a / 255.0})"
    }

    private fun fontWeightCss(name: String): Pair<String, String>? = when (name) {
        "Normal" -> "font-weight" to "400"
        "Medium" -> "font-weight" to "500"
        "SemiBold" -> "font-weight" to "600"
        "Bold" -> "font-weight" to "700"
        else -> null
    }

    private fun fontFamilyCss(name: String): Pair<String, String>? = when (name) {
        "Default" -> "font-family" to
            "system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif"
        "SansSerif" -> "font-family" to "sans-serif"
        "Serif" -> "font-family" to "serif"
        "Monospace" -> "font-family" to "monospace"
        else -> null
    }

    private fun textAlignCss(name: String): Pair<String, String>? = when (name) {
        "Start" -> "text-align" to "start"
        "Center" -> "text-align" to "center"
        "End" -> "text-align" to "end"
        "Justify" -> "text-align" to "justify"
        else -> null
    }

    private fun sizeCss(v: Evaluator.V): String? {
        return when (v) {
            is Evaluator.V.ObjectRef -> when (v.fqn) {
                "dev.unicompose.style.Size.FillParent" -> "100%"
                "dev.unicompose.style.Size.WrapContent" -> "auto"
                else -> null
            }
            is Evaluator.V.Float -> "${v.v}%"
            is Evaluator.V.Int -> "${v.v}%"
            else -> null
        }
    }

    /**
     * Format identically to Kotlin's `${float}` interpolation so the hashed
     * key matches what AtomicCss would produce for the same Style.
     * 16.0f → "16.0", 0.5f → "0.5". DO NOT optimize this without changing
     * AtomicCss in lockstep.
     */
    private fun Float.fmt(): String = this.toString()

    /** djb2 — bit-identical to AtomicCss.stableHash so hashes match across paths. */
    fun stableHash(input: String): String {
        var h = 5381L
        for (ch in input) h = ((h shl 5) + h + ch.code.toLong()) and 0xFFFFFFFFL
        val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder()
        var v = h
        repeat(7) {
            sb.append(alphabet[(v % alphabet.length).toInt()])
            v /= alphabet.length
        }
        return sb.toString()
    }
}
