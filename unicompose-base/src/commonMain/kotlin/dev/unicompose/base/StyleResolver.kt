package dev.unicompose.base

import dev.unicompose.style.BorderRadius
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.Padding
import dev.unicompose.style.Sp
import dev.unicompose.style.Style

/**
 * Resolves the `*Ref` fields on a [Style] against the active token set.
 *
 * For each `*Ref` slot whose value matches a [TokenRefs] entry, sets the
 * matching literal field on the returned Style. The refs themselves are
 * preserved (not cleared), so the web AtomicCss path can still emit
 * `var(--name)` while the CMP `toModifier()` path now sees the literal
 * value it needs.
 *
 * Cheap fast path when no refs are set: returns `this` unchanged.
 *
 * Why both literal + ref are preserved: on the web a var() reference is
 * preferred (so theme switching doesn't require recomposition), while on
 * CMP only literal values are used. Setting both keeps the same Style
 * instance valid on both backends.
 */
public fun Style.resolveRefs(tokens: Tokens): Style {
    if (!hasAnyRefs()) return this
    return copy(
        color = colorRef?.let { resolveColor(it, tokens) } ?: color,
        backgroundColor = backgroundColorRef?.let { resolveColor(it, tokens) } ?: backgroundColor,
        gap = gapRef?.let { resolveDp(it, tokens) } ?: gap,
        fontSize = fontSizeRef?.let { resolveSp(it, tokens) } ?: fontSize,
        padding = paddingAllRef?.let { ref ->
            resolveDp(ref, tokens)?.let { Padding.all(it) }
        } ?: padding,
        borderRadius = borderRadiusAllRef?.let { ref ->
            resolveDp(ref, tokens)?.let { BorderRadius.all(it) }
        } ?: borderRadius,
    )
}

private fun Style.hasAnyRefs(): Boolean =
    colorRef != null || backgroundColorRef != null || gapRef != null ||
        fontSizeRef != null || paddingAllRef != null || borderRadiusAllRef != null

private fun resolveColor(name: String, tokens: Tokens): Color? = when (name) {
    TokenRefs.colors.accent -> tokens.colors.accent
    TokenRefs.colors.onAccent -> tokens.colors.onAccent
    TokenRefs.colors.bgPage -> tokens.colors.bgPage
    TokenRefs.colors.bgSurface -> tokens.colors.bgSurface
    TokenRefs.colors.bgSubtle -> tokens.colors.bgSubtle
    TokenRefs.colors.textPrimary -> tokens.colors.textPrimary
    TokenRefs.colors.textSecondary -> tokens.colors.textSecondary
    TokenRefs.colors.borderSubtle -> tokens.colors.borderSubtle
    TokenRefs.colors.error -> tokens.colors.error
    TokenRefs.colors.success -> tokens.colors.success
    else -> null
}

private fun resolveDp(name: String, tokens: Tokens): Dp? = when (name) {
    TokenRefs.space.xs -> tokens.space.xs
    TokenRefs.space.sm -> tokens.space.sm
    TokenRefs.space.md -> tokens.space.md
    TokenRefs.space.lg -> tokens.space.lg
    TokenRefs.space.xl -> tokens.space.xl
    TokenRefs.radii.sm -> tokens.radii.sm
    TokenRefs.radii.md -> tokens.radii.md
    TokenRefs.radii.lg -> tokens.radii.lg
    else -> null
}

private fun resolveSp(name: String, tokens: Tokens): Sp? = when (name) {
    TokenRefs.type.xs -> tokens.type.xs
    TokenRefs.type.sm -> tokens.type.sm
    TokenRefs.type.md -> tokens.type.md
    TokenRefs.type.lg -> tokens.type.lg
    TokenRefs.type.xl -> tokens.type.xl
    else -> null
}
