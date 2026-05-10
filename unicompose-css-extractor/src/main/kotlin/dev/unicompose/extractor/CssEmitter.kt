package dev.unicompose.extractor

/**
 * Turns a map of statically-evaluated Style argument values into a list of
 * CSS `(property, value)` rules + a deterministic class name hash.
 *
 * Mirrors the format produced by `unicompose/src/jsMain/.../style/AtomicCss.kt`'s
 * `visualRules()` / `flexRules()` and `stableHash()` so build-time-emitted
 * classes resolve to the SAME hash that the runtime path would have produced
 * for the same Style — the runtime path stays as a fallback for non-extractable
 * styles, and identical hashes mean both paths produce the same CSS rule set.
 */
internal object CssEmitter {

    data class Emission(val className: String, val cssBody: String)

    fun emit(args: Map<String, Evaluator.V>, prefix: String = "uc"): Emission? {
        val rules = mutableListOf<Pair<String, String>>()
        for (paramName in ParamOrder) {
            val argValue = args[paramName] ?: continue
            ruleFor(paramName, argValue)?.let { rules += it }
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
     */
    private val ParamOrder = listOf(
        "padding", "margin",
        "backgroundColor", "color",
        "fontSize", "fontWeight", "fontFamily",
        "lineHeight", "letterSpacing", "textAlign",
        "borderRadius", "border", "boxShadow",
        "opacity", "width", "height", "flex",
        "gap",
    )

    private fun ruleFor(paramName: String, v: Evaluator.V): Pair<String, String>? = when (paramName) {
        "padding" -> (v as? Evaluator.V.Padding)?.let { "padding" to paddingCss(it) }
        "margin" -> (v as? Evaluator.V.Padding)?.let { "margin" to paddingCss(it) }
        "backgroundColor" -> colorCssOrNull(v)?.let { "background-color" to it }
        "color" -> colorCssOrNull(v)?.let { "color" to it }
        "gap" -> dpCssOrNull(v)?.let { "gap" to it }
        "fontSize" -> spCssOrNull(v)?.let { "font-size" to it }
        "fontWeight" -> (v as? Evaluator.V.EnumEntry)?.let { fontWeightCss(it.name) }
        "fontFamily" -> (v as? Evaluator.V.EnumEntry)?.let { fontFamilyCss(it.name) }
        "lineHeight" -> spCssOrNull(v)?.let { "line-height" to it }
        "letterSpacing" -> spCssOrNull(v)?.let { "letter-spacing" to it }
        "textAlign" -> (v as? Evaluator.V.EnumEntry)?.let { textAlignCss(it.name) }
        "borderRadius" -> (v as? Evaluator.V.BorderRadius)?.let { "border-radius" to borderRadiusCss(it) }
        "border" -> (v as? Evaluator.V.UniformBorder)?.let {
            val color = colorCssOrNull(it.color) ?: return null
            "border" to "${it.widthDp.fmt()}px solid $color"
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

    /** Format any Color-shaped V value to a CSS string. */
    private fun colorCssOrNull(v: Evaluator.V): String? = when (v) {
        is Evaluator.V.Color -> colorCss(v.argb)
        is Evaluator.V.ColorRef -> "var(${v.cssVarName})"
        else -> null
    }

    /** Format any Dp-shaped V value as a CSS length. */
    private fun dpCssOrNull(v: Evaluator.V): String? = when (v) {
        is Evaluator.V.Dp -> "${v.v.fmt()}px"
        is Evaluator.V.DpRef -> "var(${v.cssVarName})"
        else -> null
    }

    /** Format any Sp-shaped V value as a CSS length. */
    private fun spCssOrNull(v: Evaluator.V): String? = when (v) {
        is Evaluator.V.Sp -> "${v.v.fmt()}px"
        is Evaluator.V.SpRef -> "var(${v.cssVarName})"
        else -> null
    }

    /**
     * CSS shorthand for padding/margin: emit fewer values when sides are equal
     * (matches what AtomicCss's [Padding].toCss() does at runtime).
     */
    private fun paddingCss(p: Evaluator.V.Padding): String {
        val t = dpCssOrNull(p.top) ?: return "0"
        val r = dpCssOrNull(p.right) ?: return "0"
        val b = dpCssOrNull(p.bottom) ?: return "0"
        val l = dpCssOrNull(p.left) ?: return "0"
        if (t == r && r == b && b == l) return t
        if (t == b && l == r) return "$t $l"
        return "$t $r $b $l"
    }

    private fun borderRadiusCss(br: Evaluator.V.BorderRadius): String {
        val tl = dpCssOrNull(br.tl) ?: return "0"
        val tr = dpCssOrNull(br.tr) ?: return "0"
        val brc = dpCssOrNull(br.br) ?: return "0"
        val bl = dpCssOrNull(br.bl) ?: return "0"
        if (tl == tr && tr == brc && brc == bl) return tl
        return "$tl $tr $brc $bl"
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

    private fun sizeCss(v: Evaluator.V): String? = when (v) {
        is Evaluator.V.ObjectRef -> when (v.fqn) {
            "dev.unicompose.style.Size.FillParent" -> "100%"
            "dev.unicompose.style.Size.WrapContent" -> "auto"
            else -> null
        }
        is Evaluator.V.Float -> "${v.v}%"
        is Evaluator.V.Int -> "${v.v}%"
        else -> null
    }

    /**
     * Format identically to Kotlin's `${float}` interpolation so the hashed
     * key matches what AtomicCss would produce for the same Style.
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
