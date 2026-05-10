package dev.unicompose.base

import dev.unicompose.style.Border
import dev.unicompose.style.BorderEdge
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.Margin
import dev.unicompose.style.Padding
import dev.unicompose.style.Shadow
import dev.unicompose.style.Sp
import dev.unicompose.style.Style

/**
 * Resolves token references against the active token set so the returned
 * [Style] only contains literal values. Used by themed widgets on every
 * platform — on the web the resolved Style still works (literal colors
 * format as `rgb(...)`); on CMP the literal values flow through to the
 * Compose Modifier chain that has no concept of CSS variables.
 *
 * Cheap fast-path: returns `this` unchanged when no refs are present.
 *
 * Walks every typed-value slot for refs:
 *  - [Color] in color, backgroundColor, border edges, boxShadow.color
 *  - [Dp] in gap, padding (each side), margin, borderRadius (each corner),
 *    boxShadow offsets / blur / spread, border edge widths
 *  - [Sp] in fontSize, lineHeight, letterSpacing
 */
public fun Style.resolveRefs(tokens: Tokens): Style {
    if (!hasAnyRefs()) return this
    return copy(
        color = color?.resolved(tokens),
        backgroundColor = backgroundColor?.resolved(tokens),
        padding = padding?.resolved(tokens),
        margin = margin?.resolved(tokens),
        borderRadius = borderRadius?.resolved(tokens),
        border = border?.resolved(tokens),
        boxShadow = boxShadow?.resolved(tokens),
        gap = gap?.resolved(tokens),
        fontSize = fontSize?.resolved(tokens),
        lineHeight = lineHeight?.resolved(tokens),
        letterSpacing = letterSpacing?.resolved(tokens),
    )
}

private fun Style.hasAnyRefs(): Boolean =
    color is Color.Ref || backgroundColor is Color.Ref ||
        gap is Dp.Ref || fontSize is Sp.Ref ||
        lineHeight is Sp.Ref || letterSpacing is Sp.Ref ||
        padding.hasRef() || margin.hasRef() || borderRadius.hasRef() ||
        border.hasRef() || boxShadow.hasRef()

private fun Padding?.hasRef(): Boolean =
    this != null && (top is Dp.Ref || right is Dp.Ref || bottom is Dp.Ref || left is Dp.Ref)

private fun Margin?.hasRef(): Boolean =
    this != null && (top is Dp.Ref || right is Dp.Ref || bottom is Dp.Ref || left is Dp.Ref)

private fun BorderRadius?.hasRef(): Boolean =
    this != null && (topLeft is Dp.Ref || topRight is Dp.Ref || bottomRight is Dp.Ref || bottomLeft is Dp.Ref)

private fun Border?.hasRef(): Boolean = this != null && (
    top.hasRef() || right.hasRef() || bottom.hasRef() || left.hasRef())

private fun BorderEdge?.hasRef(): Boolean =
    this != null && (color is Color.Ref || width is Dp.Ref)

private fun Shadow?.hasRef(): Boolean = this != null && (
    color is Color.Ref || offsetX is Dp.Ref || offsetY is Dp.Ref ||
        blur is Dp.Ref || spread is Dp.Ref)

private fun Color.resolved(tokens: Tokens): Color = when (this) {
    is Color.Literal -> this
    is Color.Ref -> resolveColor(cssVarName, tokens) ?: Color.Transparent
}

private fun Dp.resolved(tokens: Tokens): Dp = when (this) {
    is Dp.Literal -> this
    is Dp.Ref -> resolveDp(cssVarName, tokens) ?: Dp.Zero
}

private fun Sp.resolved(tokens: Tokens): Sp = when (this) {
    is Sp.Literal -> this
    is Sp.Ref -> resolveSp(cssVarName, tokens) ?: Sp.Literal(0f)
}

private fun Padding.resolved(tokens: Tokens): Padding = Padding(
    top = top.resolved(tokens),
    right = right.resolved(tokens),
    bottom = bottom.resolved(tokens),
    left = left.resolved(tokens),
)

private fun Margin.resolved(tokens: Tokens): Margin = Margin(
    top = top.resolved(tokens),
    right = right.resolved(tokens),
    bottom = bottom.resolved(tokens),
    left = left.resolved(tokens),
)

private fun BorderRadius.resolved(tokens: Tokens): BorderRadius = BorderRadius(
    topLeft = topLeft.resolved(tokens),
    topRight = topRight.resolved(tokens),
    bottomRight = bottomRight.resolved(tokens),
    bottomLeft = bottomLeft.resolved(tokens),
)

private fun Border.resolved(tokens: Tokens): Border = Border(
    top = top?.resolved(tokens),
    right = right?.resolved(tokens),
    bottom = bottom?.resolved(tokens),
    left = left?.resolved(tokens),
)

private fun BorderEdge.resolved(tokens: Tokens): BorderEdge =
    BorderEdge(width = width.resolved(tokens), color = color.resolved(tokens))

private fun Shadow.resolved(tokens: Tokens): Shadow = Shadow(
    offsetX = offsetX.resolved(tokens),
    offsetY = offsetY.resolved(tokens),
    blur = blur.resolved(tokens),
    spread = spread.resolved(tokens),
    color = color.resolved(tokens),
)

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
