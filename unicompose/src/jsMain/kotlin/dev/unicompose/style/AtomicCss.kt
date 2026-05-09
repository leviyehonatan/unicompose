package dev.unicompose.style

import kotlinx.browser.document
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
        (document.createElement("style") as HTMLStyleElement).also { el ->
            el.id = "unicompose-styles"
            document.head?.appendChild(el)
        }
    }

    /** Class name for a non-flex element (visual properties only). */
    fun classFor(style: Style): String = register(visualRules(style), prefix = "uc")

    /** Class name for a flex container (visual + flex layout properties). */
    fun classForFlexContainer(style: Style): String =
        register(visualRules(style) + flexRules(style), prefix = "ucf")

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
        style.padding?.let { p ->
            out += "padding" to "${p.top.value}px ${p.right.value}px ${p.bottom.value}px ${p.left.value}px"
        }
        style.margin?.let { m ->
            out += "margin" to "${m.top.value}px ${m.right.value}px ${m.bottom.value}px ${m.left.value}px"
        }
        style.backgroundColor?.let { out += "background-color" to it.toCss() }
        style.color?.let { out += "color" to it.toCss() }
        style.fontSize?.let { out += "font-size" to "${it.value}px" }
        style.fontWeight?.let { out += "font-weight" to it.value.toString() }
        style.borderRadius?.let { out += "border-radius" to "${it.value}px" }
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
        style.gap?.let { out += "gap" to "${it.value}px" }
        return out
    }
}

private fun Color.toCss(): String =
    if (alpha == 0xFF) "rgb($red,$green,$blue)" else "rgba($red,$green,$blue,${alpha / 255.0})"

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
