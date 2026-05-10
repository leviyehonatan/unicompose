package dev.unicompose.base

import dev.unicompose.style.Border
import dev.unicompose.style.BorderEdge
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.Padding
import dev.unicompose.style.Shadow
import dev.unicompose.style.Sp
import dev.unicompose.style.Style
import dev.unicompose.style.dp

/**
 * Resolves token references against the active token set so the returned
 * [Style] only contains literal values. Used by themed widgets on every
 * platform — on the web the resolved Style still works (literal colors
 * format as `rgb(...)`); on CMP the literal values flow through to the
 * Compose Modifier chain that has no concept of CSS variables.
 *
 * Cheap fast-path: returns `this` unchanged when no refs are present.
 *
 * Resolves:
 *  - [Style.color] / [Style.backgroundColor] when they're [Color.Ref]
 *  - All four sides' colors on [Style.border]
 *  - The color on [Style.boxShadow]
 *  - The remaining `*Ref: String?` fields for Dp/Sp slots that don't yet
 *    have the sealed-interface treatment (gap, fontSize, padding,
 *    borderRadius). When those types follow Color into sealed interfaces,
 *    those fields disappear too and this whole helper shrinks further.
 */
public fun Style.resolveRefs(tokens: Tokens): Style {
    if (!hasAnyRefs()) return this
    return copy(
        color = color?.resolved(tokens),
        backgroundColor = backgroundColor?.resolved(tokens),
        border = border?.resolved(tokens),
        boxShadow = boxShadow?.resolved(tokens),
        gap = gapRef?.let { resolveDp(it, tokens) } ?: gap,
        fontSize = fontSizeRef?.let { resolveSp(it, tokens) } ?: fontSize,
        padding = when {
            paddingAllRef != null -> resolveDp(paddingAllRef!!, tokens)?.let { Padding.all(it) } ?: padding
            paddingVerticalRef != null || paddingHorizontalRef != null -> {
                val v = paddingVerticalRef?.let { resolveDp(it, tokens) } ?: 0.dp
                val h = paddingHorizontalRef?.let { resolveDp(it, tokens) } ?: 0.dp
                Padding.symmetric(vertical = v, horizontal = h)
            }
            else -> padding
        },
        borderRadius = borderRadiusAllRef?.let { ref ->
            resolveDp(ref, tokens)?.let { BorderRadius.all(it) }
        } ?: borderRadius,
    )
}

// Returns true if any value in the Style refers to a token that needs resolving.
// Walks the Color slots inside Border and Shadow so we catch nested refs too.
private fun Style.hasAnyRefs(): Boolean {
    if (gapRef != null || fontSizeRef != null || paddingAllRef != null ||
        borderRadiusAllRef != null || paddingVerticalRef != null || paddingHorizontalRef != null
    ) return true
    if (color is Color.Ref || backgroundColor is Color.Ref) return true
    val b = border
    if (b != null) {
        if (b.top?.color is Color.Ref) return true
        if (b.right?.color is Color.Ref) return true
        if (b.bottom?.color is Color.Ref) return true
        if (b.left?.color is Color.Ref) return true
    }
    if (boxShadow?.color is Color.Ref) return true
    return false
}

/** Resolves a [Color.Ref] against [tokens]. Pass-through for [Color.Literal]. */
private fun Color.resolved(tokens: Tokens): Color = when (this) {
    is Color.Literal -> this
    is Color.Ref -> resolveColor(cssVarName, tokens) ?: Color.Transparent
}

private fun Border.resolved(tokens: Tokens): Border = Border(
    top = top?.resolved(tokens),
    right = right?.resolved(tokens),
    bottom = bottom?.resolved(tokens),
    left = left?.resolved(tokens),
)

private fun BorderEdge.resolved(tokens: Tokens): BorderEdge =
    if (color is Color.Ref) BorderEdge(width = width, color = color.resolved(tokens)) else this

private fun Shadow.resolved(tokens: Tokens): Shadow =
    if (color is Color.Ref) copy(color = color.resolved(tokens)) else this

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
