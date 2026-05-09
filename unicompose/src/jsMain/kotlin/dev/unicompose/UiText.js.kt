package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
public actual fun UiText(text: String, style: Style) {
    val cls = AtomicCss.classFor(style)
    Span(attrs = { classes(cls) }) {
        Text(text)
    }
}
