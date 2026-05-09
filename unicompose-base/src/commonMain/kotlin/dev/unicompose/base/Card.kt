package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.UiBox
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.Padding
import dev.unicompose.style.Style

/**
 * A surface-shaped container themed by the active [Tokens].
 *
 * Wraps the underlying [UiBox] primitive with default background, padding, and
 * corner radius pulled from [currentTokens]. Pass [style] to override individual
 * properties at the call site.
 *
 * Lives in `unicompose-base` (not `unicompose`) because cards are a design
 * opinion, not a primitive. The mechanism layer doesn't ship `Card`.
 *
 * @param style Overrides layered on top of [CardDefaults.style].
 * @param content Children rendered inside the card. Cards default to a vertical
 *   flex layout; pass `flexDirection = Row` on [style] to lay them horizontally.
 *
 * @sample
 * ```
 * Card {
 *     Text("Title", style = Style(fontWeight = FontWeight.SemiBold))
 *     Text("Body copy.")
 * }
 * ```
 */
@Composable
public fun Card(style: Style = Style.Empty, content: @Composable () -> Unit) {
    UiBox(style = CardDefaults.style() + style, content = content)
}

/** Default style helpers for [Card]. Namespaced object follows the Compose convention. */
public object CardDefaults {
    /**
     * The default styling layered under user-provided overrides in [Card],
     * resolved from the active token set.
     *
     * - background: `colors.bgSurface`
     * - padding: `space.md` on all sides
     * - corner radius: `radii.lg`
     */
    @Composable
    public fun style(): Style {
        val t = currentTokens()
        return Style(
            backgroundColor = t.colors.bgSurface,
            padding = Padding.all(t.space.md),
            borderRadius = BorderRadius.all(t.radii.lg),
        )
    }
}
