package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.argb

/**
 * A thin separator line — horizontal by default, vertical if [vertical] is true.
 *
 * Backed by:
 *  - A semantic `<hr>` element on Compose HTML for accessibility (screen readers
 *    announce it as a separator). The `<hr>` is styled to match [color] and [thickness].
 *  - A solid-color `Box` of the requested thickness on Compose Multiplatform.
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

/** Default divider color — 12% opaque black. */
public val DefaultDividerColor: Color = argb(31, 0, 0, 0)

/** Default divider thickness — 1dp. */
public val DefaultDividerThickness: Dp = Dp(1f)
