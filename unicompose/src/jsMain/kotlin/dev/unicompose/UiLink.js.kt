package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Text

@Composable
public actual fun UiLink(text: String, href: String, style: Style) {
    val cls = AtomicCss.classFor(style)
    A(href = href, attrs = { classes(cls) }) {
        Text(text)
    }
}
