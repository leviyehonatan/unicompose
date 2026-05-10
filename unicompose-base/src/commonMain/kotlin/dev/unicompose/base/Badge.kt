package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.ProvideDefaultTextColor
import dev.unicompose.UiBox
import dev.unicompose.UiText
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Padding
import dev.unicompose.style.Sp
import dev.unicompose.style.Style
import dev.unicompose.style.dp

/**
 * Small inline indicator with a string label — count, status pill, or tag.
 *
 * For badges with mixed inline content (icon + text), use the content-lambda
 * overload below.
 *
 * @sample
 * ```
 * Badge("3 new")
 * Badge("Beta", style = Style(backgroundColor = currentTokens().colors.accent))
 * ```
 */
@Composable
public fun Badge(text: String, style: Style = Style.Empty) {
    Badge(style = style) { UiText(text) }
}

/**
 * Small inline indicator with arbitrary inline content (Kobweb-style).
 *
 * Default styling pulled from the active [Tokens]; the foreground color is
 * propagated via [ProvideDefaultTextColor], so a bare `Text("...")` in
 * [content] picks up the badge's text color.
 *
 * @param style Overrides applied on top of [BadgeDefaults.style].
 * @param content Badge content; typically `Text("...")`.
 *
 * @sample
 * ```
 * Badge { Text("3 new") }
 * ```
 */
@Composable
public fun Badge(
    style: Style = Style.Empty,
    content: @Composable () -> Unit,
) {
    val merged = (BadgeStyle + style).resolveRefs(currentTokens())
    UiBox(style = merged) {
        ProvideDefaultTextColor(merged.color, content)
    }
}

/**
 * Default styling layered under user-provided overrides in [Badge]. Top-level
 * val so the IR plugin extracts it. Mixes a literal vertical padding (2 dp —
 * pills are always tightly fit, not theme-driven) with a token horizontal
 * padding (`space.sm`); the new sealed-interface Dp lets that ride in a
 * single `Padding.symmetric(...)` without parallel `*Ref` shims.
 */
public val BadgeStyle: Style = Style(
    backgroundColor = Color.token(TokenRefs.colors.bgSubtle),
    color = Color.token(TokenRefs.colors.textPrimary),
    // 999dp = full pill — pills are always fully round regardless of theme.
    borderRadius = BorderRadius.all(999.dp),
    padding = Padding.symmetric(
        vertical = 2.dp,
        horizontal = Dp.token(TokenRefs.space.sm),
    ),
    fontSize = Sp.token(TokenRefs.type.xs),
    fontWeight = FontWeight.Medium,
)

/** Default style helpers for [Badge]. Backwards-compatible API. */
public object BadgeDefaults {
    /** Backwards-compatible accessor — returns the same [BadgeStyle] constant. */
    @Composable
    public fun style(): Style = BadgeStyle
}
