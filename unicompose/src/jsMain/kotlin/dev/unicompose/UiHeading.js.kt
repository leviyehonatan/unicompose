package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text

@Composable
public actual fun UiHeading(level: HeadingLevel, text: String, style: Style) {
    val merged = defaultHeadingStyle(level) + style
    val cls = AtomicCss.classFor(merged)
    when (level) {
        HeadingLevel.H1 -> H1(attrs = { classes(cls) }) { Text(text) }
        HeadingLevel.H2 -> H2(attrs = { classes(cls) }) { Text(text) }
        HeadingLevel.H3 -> H3(attrs = { classes(cls) }) { Text(text) }
    }
}
