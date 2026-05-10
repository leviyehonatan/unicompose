package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.UiBox
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
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
    UiBox(style = (CardStyles.default + style).resolveRefs(currentTokens()), content = content)
}

/**
 * Card style variants, namespaced for autocomplete + co-location. Each member
 * is a top-level Style declaration the IR plugin extracts statically.
 *
 * In v0.1 there's only [default]; future variants (e.g. `elevated`, `outlined`)
 * land here without disturbing call sites.
 *
 * - background: `colors.bgSurface`
 * - padding: `space.md` on all sides
 * - corner radius: `radii.lg`
 */
public object CardStyles {
    public val default: Style = Style(
        backgroundColor = Color.token(TokenRefs.colors.bgSurface),
        padding = Padding.all(Dp.token(TokenRefs.space.md)),
        borderRadius = BorderRadius.all(Dp.token(TokenRefs.radii.lg)),
    )
}
