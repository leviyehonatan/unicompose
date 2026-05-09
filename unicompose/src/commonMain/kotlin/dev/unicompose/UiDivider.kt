package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Color
import dev.unicompose.style.Dp

/**
 * A thin separator line — horizontal by default, vertical if [vertical] is true.
 *
 * Backed by:
 *  - A semantic `<hr>` element on Compose HTML for accessibility (screen readers
 *    announce it as a separator). The `<hr>` is styled to match [color] and [thickness].
 *  - A solid-color `Box` of the requested thickness on Compose Multiplatform.
 *
 * Defaults are in [DefaultDividerColor] and [DefaultDividerThickness].
 *
 * @param color Stroke color. Defaults to a 12%-opaque black, a typical divider tone.
 * @param thickness Stroke width perpendicular to the divider's main axis.
 * @param vertical When true, the divider runs top-to-bottom and is sized via [thickness]
 *   on the X axis. Default is horizontal.
 */
@Composable
public expect fun UiDivider(
    color: Color = DefaultDividerColor,
    thickness: Dp = DefaultDividerThickness,
    vertical: Boolean = false,
)
