package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Color
import dev.unicompose.style.Padding
import dev.unicompose.style.Style
import dev.unicompose.style.dp

/**
 * A surface-shaped container with default padding, background, and rounded corners.
 *
 * Pure commonMain wrapper over [UiBox] — no platform-specific actual is needed.
 * The default style is layered first, with [style] overriding individual properties.
 *
 * Cards do not draw shadows in v0.1 (the [Style] surface does not yet expose a
 * shadow property). Use a subtle border or background tint for visual separation.
 *
 * @param style Style overrides layered on top of the default card style.
 * @param content Children rendered inside the card. Cards default to a vertical
 *   flex layout, so children stack top-to-bottom; pass `flexDirection = Row`
 *   on [style] to lay them horizontally.
 *
 * @sample
 * ```
 * UiCard {
 *     UiHeading(HeadingLevel.H3, "Title")
 *     UiText("Body copy.")
 * }
 * ```
 */
@Composable
public fun UiCard(style: Style = Style.Empty, content: @Composable () -> Unit) {
    UiBox(style = DefaultCardStyle + style, content = content)
}

/** The default styling layered under user-provided overrides in [UiCard]. */
public val DefaultCardStyle: Style = Style(
    backgroundColor = Color.White,
    padding = Padding.all(16.dp),
    borderRadius = 12.dp,
)
