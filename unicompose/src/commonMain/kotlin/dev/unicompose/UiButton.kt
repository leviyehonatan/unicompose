package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

/**
 * A clickable container with semantic button affordance.
 *
 * Backed by:
 *  - `<button type="button">` on Compose HTML — accessible to screen readers,
 *    keyboard-navigable, fires on Enter/Space.
 *  - A `Box` with `Modifier.clickable` on Compose Multiplatform.
 *
 * Browser-default `<button>` styling (gray background, raised border) is reset on
 * web so [style] is the single source of truth across platforms.
 *
 * Buttons are unstyled by default — provide colors, padding, and typography via
 * [style]. The contained [content] lambda renders the button's label and any
 * other inner widgets.
 *
 * @param onClick Invoked when the user clicks / taps / presses Enter on the button.
 * @param style Visual and layout styling for the button.
 * @param enabled When false, click events are suppressed and the button is dimmed
 *   to half opacity. Default true.
 * @param content Composable content rendered inside the button.
 *
 * @sample
 * ```
 * UiButton(
 *     onClick = { /* handle */ },
 *     style = Style(
 *         backgroundColor = Accent,
 *         padding = Padding.symmetric(vertical = 10.dp, horizontal = 16.dp),
 *         borderRadius = 8.dp,
 *     ),
 * ) {
 *     UiText("Save", style = Style(color = Color.White))
 * }
 * ```
 */
@Composable
public expect fun UiButton(
    onClick: () -> Unit,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
)
