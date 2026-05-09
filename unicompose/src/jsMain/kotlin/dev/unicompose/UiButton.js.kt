package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.dom.Button

@Composable
public actual fun UiButton(
    onClick: () -> Unit,
    style: Style,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    // Apply opacity reduction when disabled by merging into the style — keeps the
    // styling unified inside the atomic class rather than fighting via inline style.
    val effective = if (enabled) style else style + Style(opacity = 0.5f)
    val cls = AtomicCss.classForButton(effective)
    Button(attrs = {
        type(ButtonType.Button)
        classes(cls)
        if (enabled) {
            onClick { onClick() }
        } else {
            disabled()
            // Override the reset's default cursor for clearer affordance.
            attr("style", "cursor:not-allowed")
        }
    }) {
        content()
    }
}
