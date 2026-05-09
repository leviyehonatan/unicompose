package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.HeadingLevel
import dev.unicompose.UiHeading
import dev.unicompose.style.Style

/**
 * A semantic heading themed by the active [Tokens].
 *
 * Wraps the underlying [UiHeading] primitive with default text color drawn from
 * `colors.textPrimary`. The level's intrinsic size and weight (set by `unicompose`'s
 * `defaultHeadingStyle`) carry over; [style] overrides layer on top of both.
 *
 * Lives in `unicompose-base` because the color choice is a design opinion. The
 * underlying `UiHeading` in `unicompose` keeps the system text color so the
 * primitive remains usable without a theme.
 *
 * @param level The semantic level — see [HeadingLevel] in unicompose.
 * @param text The heading content.
 * @param style Visual overrides layered on top of token defaults.
 *
 * @sample
 * ```
 * Heading(HeadingLevel.H1, "unicompose")
 * Heading(HeadingLevel.H2, "Quick start", style = Style(color = currentTokens().colors.textSecondary))
 * ```
 */
@Composable
public fun Heading(
    level: HeadingLevel,
    text: String,
    style: Style = Style.Empty,
) {
    UiHeading(level = level, text = text, style = HeadingDefaults.style() + style)
}

/** Default style helpers for [Heading]. */
public object HeadingDefaults {
    /**
     * Default heading style, resolved from the active token set. Just sets
     * `color = colors.textPrimary`; size and weight come from the level's
     * defaults in `unicompose`'s `defaultHeadingStyle`.
     */
    @Composable
    public fun style(): Style = Style(color = currentTokens().colors.textPrimary)
}
