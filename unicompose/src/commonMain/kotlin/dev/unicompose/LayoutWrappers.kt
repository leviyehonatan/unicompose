package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.FlexDirection
import dev.unicompose.style.Style

/**
 * A horizontal flex container — children laid out left-to-right.
 *
 * Equivalent to [UiBox] with [Style.flexDirection] forced to [FlexDirection.Row].
 * Any direction set on [style] is ignored. All other style properties (gap,
 * alignment, justification, padding, etc.) are honored.
 *
 * @sample
 * ```
 * UiRow(style = Style(gap = 8.dp, alignItems = Align.Center)) {
 *     UiText("left")
 *     UiText("right")
 * }
 * ```
 */
@Composable
public fun UiRow(style: Style = Style.Empty, content: @Composable () -> Unit) {
    UiBox(style.copy(flexDirection = FlexDirection.Row), content)
}

/**
 * A vertical flex container — children laid out top-to-bottom.
 *
 * Equivalent to [UiBox] with [Style.flexDirection] forced to [FlexDirection.Column].
 * Any direction set on [style] is ignored. All other style properties are honored.
 *
 * @sample
 * ```
 * UiColumn(style = Style(gap = 12.dp)) {
 *     UiText("title")
 *     UiText("body")
 * }
 * ```
 */
@Composable
public fun UiColumn(style: Style = Style.Empty, content: @Composable () -> Unit) {
    UiBox(style.copy(flexDirection = FlexDirection.Column), content)
}
