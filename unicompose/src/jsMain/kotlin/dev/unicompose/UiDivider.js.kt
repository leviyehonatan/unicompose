package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.Size
import dev.unicompose.style.Style
import org.jetbrains.compose.web.dom.Hr

@Composable
public actual fun UiDivider(color: Color, thickness: Dp, vertical: Boolean) {
    val style = if (vertical) {
        Style(backgroundColor = color, width = Size.Fixed(thickness), height = Size.FillParent)
    } else {
        Style(backgroundColor = color, height = Size.Fixed(thickness), width = Size.FillParent)
    }
    val cls = AtomicCss.classFor(style)
    Hr(attrs = {
        classes(cls)
        // <hr> ships with browser-default border + margin; reset so our class controls everything.
        attr("style", "border:0;margin:0;padding:0")
    })
}
