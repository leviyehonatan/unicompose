package dev.unicompose.style

import kotlinx.browser.document
import org.w3c.dom.HTMLLinkElement
import org.w3c.dom.HTMLStyleElement

/**
 * Atomic CSS class generator — the styling backend for unicompose on the web.
 *
 * Each unique [Style] is hashed into a deterministic class name and registered into
 * a singleton `<style id="unicompose-styles">` element on first use. Subsequent
 * usages reuse the cached class, so generated CSS is deduplicated and cacheable
 * by the browser.
 *
 * Two emission modes: [classFor] for non-layout elements (text, etc.) and
 * [classForFlexContainer] for elements that should be flex containers.
 *
 * Design note: extraction happens at runtime, not at build time. A KSP-based
 * compile-time extractor was considered but is not on the roadmap — the runtime
 * version produces identical DOM, hashes are stable across runs, and the
 * first-paint cost is sub-millisecond for typical apps. Build-time extraction
 * would only pay off for cold-start-critical apps with thousands of unique styles
 * or for SSR (which unicompose does not target).
 */
internal object AtomicCss {
    private val cache = HashMap<String, String>()
    private val styleEl: HTMLStyleElement by lazy {
        // Inject the static resets sheet (unicompose-reset.css) and the build-
        // time-extracted classes sheet (unicompose-generated.css) as <link>
        // elements next to the JS bundle. Both are real .css assets — the
        // browser can cache them, render before JS executes, and show them in
        // DevTools as normal stylesheets.
        ensureLink("unicompose-reset", "unicompose-reset.css")
        ensureLink("unicompose-generated", "unicompose-generated.css")
        // Then create the runtime <style> for any per-Style atomic class that
        // the build-time extractor couldn't see (dynamic styles). Hashes match
        // what the extractor would have produced, so calling classFor() on a
        // Style the extractor saw is a no-op against the linked sheet.
        (document.createElement("style") as HTMLStyleElement).also { el ->
            el.id = "unicompose-styles"
            document.head?.appendChild(el)
        }
    }

    private fun ensureLink(id: String, href: String) {
        if (document.getElementById(id) != null) return
        (document.createElement("link") as HTMLLinkElement).also { link ->
            link.id = id
            link.rel = "stylesheet"
            link.href = href
            document.head?.appendChild(link)
        }
    }

    /** Class name for a non-flex element (visual properties only). */
    fun classFor(style: Style): String = register(visualRules(style), prefix = "uc")

    /** Class name for a flex container (visual + flex layout properties). */
    fun classForFlexContainer(style: Style): String =
        register(visualRules(style) + flexRules(style), prefix = "ucf")

    /**
     * Class name for a `<button>` element with browser-default styling reset.
     * Reset rules come first so later user-provided properties override them
     * via CSS cascade (e.g. `background:none` then `background-color:blue` → blue).
     */
    fun classForButton(style: Style): String =
        register(buttonResetRules + visualRules(style), prefix = "ucb")

    /** Class name for an `<input>` / form control with browser-default styling reset. */
    fun classForInput(style: Style): String =
        register(inputResetRules + visualRules(style), prefix = "uci")

    private val buttonResetRules: List<Pair<String, String>> = listOf(
        "border" to "0",
        "background" to "none",
        "font" to "inherit",
        "color" to "inherit",
        "padding" to "0",
        "margin" to "0",
        "cursor" to "pointer",
        "outline" to "none",
        "text-align" to "inherit",
    )

    // Mirrors the CMP UiTextField primitive's defaults so DOM and CMP
    // render the same browser-default-ish text input. Border, radius, and
    // padding are kept in lock-step with the CMP-side constants in
    // composeAppMain/UiTextField.kt; if you tune one, tune the other.
    private val inputResetRules: List<Pair<String, String>> = listOf(
        "font" to "inherit",
        "outline" to "none",
        "border" to "1px solid #8A8D91",
        "border-radius" to "4px",
        "padding" to "6px 8px",
        "background" to "white",
        "color" to "inherit",
    )

    private fun register(rules: List<Pair<String, String>>, prefix: String): String {
        if (rules.isEmpty()) return "$prefix-empty"
        val key = prefix + "|" + rules.joinToString(";") { "${it.first}:${it.second}" }
        cache[key]?.let { return it }
        val className = "$prefix-${stableHash(key)}"
        cache[key] = className
        val cssBody = rules.joinToString(";") { "${it.first}:${it.second}" }
        styleEl.appendChild(
            document.createTextNode(".$className{$cssBody}"),
        )
        return className
    }

    private fun visualRules(style: Style): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        style.padding?.let { p -> out += "padding" to p.toCss() }
        style.margin?.let { m -> out += "margin" to m.toCss() }
        // Color values are themable via Color.Ref; toCss() handles both
        // variants (literal → rgb(...) / rgba(...); ref → var(--name)).
        style.backgroundColor?.let { out += "background-color" to it.toCss() }
        style.backgroundGradient?.let { g -> out += "background-image" to g.toCss() }
        style.color?.let { out += "color" to it.toCss() }
        style.fontSize?.let { out += "font-size" to it.toCss() }
        style.fontWeight?.let { out += "font-weight" to it.value.toString() }
        style.fontFamily?.let {
            out += "font-family" to when (it) {
                FontFamily.Default -> "system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif"
                FontFamily.SansSerif -> "sans-serif"
                FontFamily.Serif -> "serif"
                FontFamily.Monospace -> "monospace"
            }
        }
        style.lineHeight?.let { out += "line-height" to it.toCss() }
        style.letterSpacing?.let { out += "letter-spacing" to it.toCss() }
        style.textAlign?.let {
            out += "text-align" to when (it) {
                TextAlign.Start -> "start"
                TextAlign.Center -> "center"
                TextAlign.End -> "end"
                TextAlign.Justify -> "justify"
            }
        }
        style.borderRadius?.let { out += "border-radius" to it.toCss() }
        style.border?.let { b ->
            if (b.isUniform) {
                val e = b.top!!
                out += "border" to "${e.width.toCss()} solid ${e.color.toCss()}"
            } else {
                fun edgeRule(side: String, e: BorderEdge?) {
                    val rule = if (e != null) "${e.width.toCss()} solid ${e.color.toCss()}" else "0"
                    out += "border-$side" to rule
                }
                edgeRule("top", b.top)
                edgeRule("right", b.right)
                edgeRule("bottom", b.bottom)
                edgeRule("left", b.left)
            }
        }
        style.boxShadow?.let { s ->
            out += "box-shadow" to
                "${s.offsetX.toCss()} ${s.offsetY.toCss()} ${s.blur.toCss()} ${s.spread.toCss()} ${s.color.toCss()}"
        }
        style.opacity?.let { out += "opacity" to it.toString() }
        when (val w = style.width) {
            is Size.Fixed -> out += "width" to w.dp.toCss()
            Size.FillParent -> out += "width" to "100%"
            is Size.Fraction -> out += "width" to "${w.value * 100}%"
            Size.WrapContent -> out += "width" to "auto"
            null -> {}
        }
        when (val h = style.height) {
            is Size.Fixed -> out += "height" to h.dp.toCss()
            Size.FillParent -> out += "height" to "100%"
            is Size.Fraction -> out += "height" to "${h.value * 100}%"
            Size.WrapContent -> out += "height" to "auto"
            null -> {}
        }
        style.flex?.let { out += "flex" to "$it 1 0%" }
        return out
    }

    private fun flexRules(style: Style): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        out += "display" to "flex"
        out += "flex-direction" to when (style.flexDirection ?: FlexDirection.Column) {
            FlexDirection.Row -> "row"
            FlexDirection.Column -> "column"
        }
        style.justifyContent?.let {
            out += "justify-content" to when (it) {
                Justify.Start -> "flex-start"
                Justify.Center -> "center"
                Justify.End -> "flex-end"
                Justify.SpaceBetween -> "space-between"
                Justify.SpaceAround -> "space-around"
                Justify.SpaceEvenly -> "space-evenly"
            }
        }
        out += "align-items" to when (style.alignItems) {
            null, Align.Start -> "flex-start"
            Align.Center -> "center"
            Align.End -> "flex-end"
            Align.Stretch -> "stretch"
        }
        style.gap?.let { out += "gap" to it.toCss() }
        return out
    }
}

private fun Color.toCss(): String = when (this) {
    is Color.Literal ->
        if (alpha == 0xFF) "rgb($red,$green,$blue)" else "rgba($red,$green,$blue,${alpha / 255.0})"
    is Color.Ref -> "var($cssVarName)"
}

/**
 * Lower a [Dp] to its CSS px representation. Refs lower to `var(--name)`;
 * literals to `${value}px`. Keeps [Float.toString]-formatted values so the
 * IR plugin's CssEmitter produces byte-identical hashes.
 */
private fun Dp.toCss(): String = when (this) {
    is Dp.Literal -> "${value}px"
    is Dp.Ref -> "var($cssVarName)"
}

private fun Sp.toCss(): String = when (this) {
    is Sp.Literal -> "${value}px"
    is Sp.Ref -> "var($cssVarName)"
}

/**
 * Lower a [Padding] to a CSS shorthand. Detects when all four sides are the
 * same Dp and emits the one-value form (`padding: var(--x)`) so the runtime
 * output matches what authors would write by hand.
 */
private fun Padding.toCss(): String {
    if (top == right && right == bottom && bottom == left) return top.toCss()
    if (top == bottom && left == right) return "${top.toCss()} ${left.toCss()}"
    return "${top.toCss()} ${right.toCss()} ${bottom.toCss()} ${left.toCss()}"
}

private fun Margin.toCss(): String {
    if (top == right && right == bottom && bottom == left) return top.toCss()
    if (top == bottom && left == right) return "${top.toCss()} ${left.toCss()}"
    return "${top.toCss()} ${right.toCss()} ${bottom.toCss()} ${left.toCss()}"
}

private fun BorderRadius.toCss(): String {
    if (topLeft == topRight && topRight == bottomRight && bottomRight == bottomLeft) {
        return topLeft.toCss()
    }
    return "${topLeft.toCss()} ${topRight.toCss()} ${bottomRight.toCss()} ${bottomLeft.toCss()}"
}

private fun LinearGradient.toCss(): String {
    val dir = when (direction) {
        GradientDirection.ToTop -> "to top"
        GradientDirection.ToBottom -> "to bottom"
        GradientDirection.ToLeft -> "to left"
        GradientDirection.ToRight -> "to right"
        GradientDirection.ToTopLeft -> "to top left"
        GradientDirection.ToTopRight -> "to top right"
        GradientDirection.ToBottomLeft -> "to bottom left"
        GradientDirection.ToBottomRight -> "to bottom right"
    }
    val localStops = stops
    val stopList = if (localStops != null) {
        colors.zip(localStops).joinToString(", ") { (c, s) -> "${c.toCss()} ${s * 100}%" }
    } else {
        colors.joinToString(", ") { it.toCss() }
    }
    return "linear-gradient($dir, $stopList)"
}

/** djb2-style hash producing a short, URL-safe alphanumeric suffix. */
private fun stableHash(input: String): String {
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
