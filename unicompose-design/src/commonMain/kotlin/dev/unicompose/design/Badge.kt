package dev.unicompose.design

import androidx.compose.runtime.Composable
import dev.unicompose.UiBox
import dev.unicompose.UiText
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Padding
import dev.unicompose.style.Style
import dev.unicompose.style.dp

/**
 * Small inline indicator — a count, status pill, or tag.
 *
 * Wraps a [UiBox] (background + pill shape) around a [UiText] (label). Defaults
 * are pulled from the active [Tokens]; [style] overrides layer on top.
 *
 * Lives in `unicompose-design` (not `unicompose`) because badges are a design
 * opinion, not a primitive.
 *
 * @param text Label inside the badge.
 * @param style Overrides applied on top of [BadgeDefaults.style].
 *
 * @sample
 * ```
 * Badge("3 new")
 * Badge("Beta", style = Style(
 *     backgroundColor = currentTokens().colors.accent,
 *     color = currentTokens().colors.onAccent,
 * ))
 * ```
 */
@Composable
public fun Badge(text: String, style: Style = Style.Empty) {
    val merged = BadgeDefaults.style() + style
    UiBox(style = merged) {
        UiText(
            text,
            style = Style(
                fontSize = merged.fontSize,
                fontWeight = merged.fontWeight,
                color = merged.color,
            ),
        )
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
            borderRadius = 999.dp,
            padding = Padding.symmetric(vertical = 2.dp, horizontal = t.space.sm),
            fontSize = t.type.xs,
            fontWeight = FontWeight.Medium,
        )
    }
}
