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
        if (style.paddingAllRef != null) {
            out += "padding" to "var(${style.paddingAllRef})"
        } else if (style.paddingVerticalRef != null || style.paddingHorizontalRef != null) {
            val v = style.paddingVerticalRef?.let { "var($it)" } ?: "0"
            val h = style.paddingHorizontalRef?.let { "var($it)" } ?: "0"
            out += "padding" to "$v $h"
        } else style.padding?.let { p ->
            out += "padding" to "${p.top.value}px ${p.right.value}px ${p.bottom.value}px ${p.left.value}px"
        }
        style.margin?.let { m ->
            out += "margin" to "${m.top.value}px ${m.right.value}px ${m.bottom.value}px ${m.left.value}px"
        }
        // Color values are themable via Color.Ref; toCss() handles both
        // variants (literal → rgb(...) / rgba(...); ref → var(--name)).
        style.backgroundColor?.let { out += "background-color" to it.toCss() }
        style.backgroundGradient?.let { g ->
            out += "background-image" to g.toCss()
        }
        style.color?.let { out += "color" to it.toCss() }
        if (style.fontSizeRef != null) {
            out += "font-size" to "var(${style.fontSizeRef})"
        } else style.fontSize?.let { out += "font-size" to "${it.value}px" }
        style.fontWeight?.let { out += "font-weight" to it.value.toString() }
        style.fontFamily?.let {
            out += "font-family" to when (it) {
                FontFamily.Default -> "system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif"
                FontFamily.SansSerif -> "sans-serif"
                FontFamily.Serif -> "serif"
                FontFamily.Monospace -> "monospace"
            }
        }
        style.lineHeight?.let { out += "line-height" to "${it.value}px" }
        style.letterSpacing?.let { out += "letter-spacing" to "${it.value}px" }
        style.textAlign?.let {
            out += "text-align" to when (it) {
                TextAlign.Start -> "start"
                TextAlign.Center -> "center"
                TextAlign.End -> "end"
                TextAlign.Justify -> "justify"
            }
        }
        if (style.borderRadiusAllRef != null) {
            out += "border-radius" to "var(${style.borderRadiusAllRef})"
        } else style.borderRadius?.let { r ->
            // CSS border-radius shorthand: top-left top-right bottom-right bottom-left
            out += "border-radius" to
                "${r.topLeft.value}px ${r.topRight.value}px ${r.bottomRight.value}px ${r.bottomLeft.value}px"
        }
        style.border?.let { b ->
            if (b.isUniform) {
                val e = b.top!!
                out += "border" to "${e.width.value}px solid ${e.color.toCss()}"
            } else {
                fun edgeRule(side: String, e: BorderEdge?) {
                    val rule = if (e != null) "${e.width.value}px solid ${e.color.toCss()}" else "0"
                    out += "border-$side" to rule
                }
                edgeRule("top", b.top)
                edgeRule("right", b.right)
                edgeRule("bottom", b.bottom)
                edgeRule("left", b.left)
            }
        }
        style.boxShadow?.let { s ->
            // CSS box-shadow: offset-x offset-y blur spread color. All four supported on web.
            out += "box-shadow" to
                "${s.offsetX.value}px ${s.offsetY.value}px ${s.blur.value}px ${s.spread.value}px ${s.color.toCss()}"
        }
        style.opacity?.let { out += "opacity" to it.toString() }
        when (val w = style.width) {
            is Size.Fixed -> out += "width" to "${w.dp.value}px"
            Size.FillParent -> out += "width" to "100%"
            is Size.Fraction -> out += "width" to "${w.value * 100}%"
            Size.WrapContent -> out += "width" to "auto"
            null -> {}
        }
        when (val h = style.height) {
            is Size.Fixed -> out += "height" to "${h.dp.value}px"
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
        // Explicit default to match Compose Row/Column default (Top/Start, NOT stretch).
        // Without this we'd inherit CSS's `align-items: stretch` default, which would
        // make web flex items expand cross-axis while CMP would top-align them.
        out += "align-items" to when (style.alignItems) {
            null, Align.Start -> "flex-start"
            Align.Center -> "center"
            Align.End -> "flex-end"
            Align.Stretch -> "stretch"
        }
        if (style.gapRef != null) {
            out += "gap" to "var(${style.gapRef})"
        } else style.gap?.let { out += "gap" to "${it.value}px" }
        return out
    }
}

private fun Color.toCss(): String = when (this) {
    is Color.Literal ->
        if (alpha == 0xFF) "rgb($red,$green,$blue)" else "rgba($red,$green,$blue,${alpha / 255.0})"
    is Color.Ref -> "var($cssVarName)"
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
