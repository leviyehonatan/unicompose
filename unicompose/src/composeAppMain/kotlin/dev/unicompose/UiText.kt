package dev.unicompose

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight as ComposeFontWeight
import androidx.compose.ui.text.style.TextAlign as ComposeTextAlign
import androidx.compose.ui.unit.sp as composeSp
import dev.unicompose.style.Style
import dev.unicompose.style.TextAlign
import dev.unicompose.style.toComposeColor
import dev.unicompose.style.toModifier

@Composable
public actual fun UiText(text: String, style: Style) {
    val inherited = currentInheritedText()
    val color = style.color ?: inherited.color
    val fontSize = style.fontSize ?: inherited.fontSize
    val fontWeight = style.fontWeight ?: inherited.fontWeight
    val lineHeight = style.lineHeight ?: inherited.lineHeight
    val letterSpacing = style.letterSpacing ?: inherited.letterSpacing
    val textAlign = style.textAlign ?: inherited.textAlign

    val base = LocalTextStyle.current
    val merged = base.copy(
        color = color?.toComposeColor() ?: ComposeColor.Unspecified,
        fontSize = fontSize?.value?.composeSp ?: base.fontSize,
        fontWeight = fontWeight?.let { ComposeFontWeight(it.value) } ?: base.fontWeight,
        lineHeight = lineHeight?.value?.composeSp ?: base.lineHeight,
        letterSpacing = letterSpacing?.value?.composeSp ?: base.letterSpacing,
        textAlign = textAlign?.toComposeTextAlign() ?: base.textAlign,
    )
    Text(
        text = text,
        modifier = style.toModifier(),
        style = merged,
    )
}

private fun TextAlign.toComposeTextAlign(): ComposeTextAlign = when (this) {
    TextAlign.Start -> ComposeTextAlign.Start
    TextAlign.Center -> ComposeTextAlign.Center
    TextAlign.End -> ComposeTextAlign.End
    TextAlign.Justify -> ComposeTextAlign.Justify
}
