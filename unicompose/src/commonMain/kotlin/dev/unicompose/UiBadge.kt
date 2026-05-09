package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Padding
import dev.unicompose.style.Style
import dev.unicompose.style.dp
import dev.unicompose.style.rgb
import dev.unicompose.style.sp

/**
 * Small inline indicator — a count, status pill, or label.
 *
 * Implemented as a [UiBox] with default pill styling (rounded, padded, low-key
 * background) plus a [UiText] for the label. The defaults are merged with
 * [style] so call sites can override colors and spacing without restating the
 * shape.
 *
 * @param text Label inside the badge.
 * @param style Overrides applied on top of the default pill style.
 *
 * @sample
 * ```
 * UiBadge("3 new")
 * UiBadge("Beta", style = Style(backgroundColor = rgb(0xEE, 0xF1, 0xFA), color = Accent))
 * ```
 */
@Composable
public fun UiBadge(text: String, style: Style = Style.Empty) {
    val defaults = Style(
        backgroundColor = rgb(0xEE, 0xEF, 0xF2),
        color = rgb(0x40, 0x44, 0x4D),
        borderRadius = 999.dp,
        padding = Padding.symmetric(vertical = 2.dp, horizontal = 8.dp),
    )
    UiBox(style = defaults + style) {
        UiText(
            text,
            style = Style(
                fontSize = (style.fontSize?.value ?: 11f).sp,
                fontWeight = style.fontWeight,
                color = style.color ?: defaults.color,
            ),
        )
    }
}
