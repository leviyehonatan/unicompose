package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.ProvideDefaultTextColor
import dev.unicompose.UiBox
import dev.unicompose.UiText
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Padding
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
    val merged = BadgeDefaults.style() + style
    UiBox(style = merged) {
        ProvideDefaultTextColor(merged.color, content)
    }
}

/** Default style helpers for [Badge]. */
public object BadgeDefaults {
    /**
     * The default styling layered under user-provided overrides in [Badge],
     * resolved from the active token set.
     *
     * - background: `colors.bgSubtle`
     * - text color: `colors.textPrimary`
     * - text size: `type.xs`, weight Medium
     * - corner radius: 999.dp (full pill — not a token; pills are always full-round)
     * - padding: 2.dp vertical / `space.sm` horizontal
     */
    @Composable
    public fun style(): Style {
        val t = currentTokens()
        return Style(
            backgroundColor = t.colors.bgSubtle,
            color = t.colors.textPrimary,
            borderRadius = BorderRadius.all(999.dp),
            padding = Padding.symmetric(vertical = 2.dp, horizontal = t.space.sm),
            fontSize = t.type.xs,
            fontWeight = FontWeight.Medium,
        )
    }
}
