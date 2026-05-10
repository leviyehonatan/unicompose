package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
public actual fun UiText(text: String, style: Style) {
    // Inherit ambient typography properties (CSS-like cascade) when this call's
    // own Style doesn't specify them. Mirrors what the CMP backend does so the
    // two render paths stay aligned.
    val inherited = currentInheritedText()
    val effective = style.copy(
        color = style.color ?: inherited.color,
        fontSize = style.fontSize ?: inherited.fontSize,
        fontWeight = style.fontWeight ?: inherited.fontWeight,
        lineHeight = style.lineHeight ?: inherited.lineHeight,
        letterSpacing = style.letterSpacing ?: inherited.letterSpacing,
        textAlign = style.textAlign ?: inherited.textAlign,
    )
    val cls = AtomicCss.classFor(effective)
    Span(attrs = { classes(cls) }) {
        Text(text)
    }
}
