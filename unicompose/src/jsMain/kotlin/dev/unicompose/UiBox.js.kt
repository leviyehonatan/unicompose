package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.dom.Div

@Composable
public actual fun UiBox(style: Style, content: @Composable () -> Unit) {
    val cls = AtomicCss.classForFlexContainer(style)
    Div(attrs = { classes(cls) }) {
        content()
    }
}
