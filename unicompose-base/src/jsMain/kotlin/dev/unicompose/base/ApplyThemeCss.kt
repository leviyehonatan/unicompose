package dev.unicompose.base

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import dev.unicompose.style.Color

/**
 * Writes the active token set as CSS custom properties on the document root.
 * Var names match those in [TokenRefs] so any `var(--uc-...)` reference
 * resolves correctly.
 *
 * Direct DOM mutation (rather than emitting a `<style>` block) keeps each
 * theme switch O(slots) and avoids re-parsing CSS. The browser's style
 * recalc on `setProperty()` calls re-resolves any `var()`-using rule
 * automatically.
 */
internal actual fun applyThemeCssVariables(tokens: Tokens) {
    val root = (document.documentElement as? HTMLElement)?.style ?: return
    val c = tokens.colors
    root.setProperty(TokenRefs.colors.accent, c.accent.toCssString())
    root.setProperty(TokenRefs.colors.onAccent, c.onAccent.toCssString())
    root.setProperty(TokenRefs.colors.bgPage, c.bgPage.toCssString())
    root.setProperty(TokenRefs.colors.bgSurface, c.bgSurface.toCssString())
    root.setProperty(TokenRefs.colors.bgSubtle, c.bgSubtle.toCssString())
    root.setProperty(TokenRefs.colors.textPrimary, c.textPrimary.toCssString())
    root.setProperty(TokenRefs.colors.textSecondary, c.textSecondary.toCssString())
    root.setProperty(TokenRefs.colors.borderSubtle, c.borderSubtle.toCssString())
    root.setProperty(TokenRefs.colors.error, c.error.toCssString())
    root.setProperty(TokenRefs.colors.success, c.success.toCssString())

    val s = tokens.space
    root.setProperty(TokenRefs.space.xs, "${s.xs.value}px")
    root.setProperty(TokenRefs.space.sm, "${s.sm.value}px")
    root.setProperty(TokenRefs.space.md, "${s.md.value}px")
    root.setProperty(TokenRefs.space.lg, "${s.lg.value}px")
    root.setProperty(TokenRefs.space.xl, "${s.xl.value}px")

    val t = tokens.type
    root.setProperty(TokenRefs.type.xs, "${t.xs.value}px")
    root.setProperty(TokenRefs.type.sm, "${t.sm.value}px")
    root.setProperty(TokenRefs.type.md, "${t.md.value}px")
    root.setProperty(TokenRefs.type.lg, "${t.lg.value}px")
    root.setProperty(TokenRefs.type.xl, "${t.xl.value}px")

    val r = tokens.radii
    root.setProperty(TokenRefs.radii.sm, "${r.sm.value}px")
    root.setProperty(TokenRefs.radii.md, "${r.md.value}px")
    root.setProperty(TokenRefs.radii.lg, "${r.lg.value}px")
}

/** Match AtomicCss.toCss() format so CSS variable values render identically.
 *  Token values are always literal — Refs would mean a token resolves to
 *  another token, which we don't support. */
private fun Color.toCssString(): String = when (this) {
    is Color.Literal ->
        if (alpha == 0xFF) "rgb($red,$green,$blue)" else "rgba($red,$green,$blue,${alpha / 255.0})"
    is Color.Ref -> "var($cssVarName)"
}
