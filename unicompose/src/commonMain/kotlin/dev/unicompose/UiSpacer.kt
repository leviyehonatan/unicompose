package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Dp
import dev.unicompose.style.Size
import dev.unicompose.style.Style

/**
 * Empty fixed-size element used to add space between widgets without applying
 * `gap` on the parent.
 *
 * Prefer `Style.gap` on the parent flex container when separating multiple
 * children uniformly; reach for [UiSpacer] when one specific gap differs from
 * the rest, or when adding flexible space (`flex` style) to push siblings apart.
 *
 * @param width Fixed horizontal size. Pass `null` to leave it intrinsic.
 * @param height Fixed vertical size. Pass `null` to leave it intrinsic.
 *
 * @sample
 * ```
 * UiRow {
 *     UiText("left")
 *     UiSpacer(width = 16.dp)
 *     UiText("right")
 * }
 * ```
 */
@Composable
public fun UiSpacer(width: Dp? = null, height: Dp? = null) {
    UiBox(
        style = Style(
            width = width?.let { Size.Fixed(it) },
            height = height?.let { Size.Fixed(it) },
        ),
    ) {}
}
