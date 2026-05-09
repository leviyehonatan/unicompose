package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

@Composable
public actual fun UiHeading(level: HeadingLevel, text: String, style: Style) {
    UiText(text = text, style = defaultHeadingStyle(level) + style)
}
