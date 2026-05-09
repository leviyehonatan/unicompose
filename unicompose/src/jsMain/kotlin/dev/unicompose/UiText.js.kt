package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
public actual fun UiText(text: String, style: Style) {
    // Inherit ambient text color when style.color is unset (CSS-like inheritance).
    val effective = if (style.color == null) {
        style.copy(color = currentDefaultTextColor())
    } else {
        style
    }
    val cls = AtomicCss.classFor(effective)
    Span(attrs = { classes(cls) }) {
        Text(text)
    }
}
