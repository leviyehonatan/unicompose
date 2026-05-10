package dev.unicompose

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily as ComposeFontFamily
import androidx.compose.ui.text.font.FontWeight as ComposeFontWeight
import androidx.compose.ui.text.style.TextAlign as ComposeTextAlign
import androidx.compose.ui.unit.sp as composeSp
import dev.unicompose.style.FontFamily
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
    val fontFamily = style.fontFamily ?: inherited.fontFamily
    val lineHeight = style.lineHeight ?: inherited.lineHeight
    val letterSpacing = style.letterSpacing ?: inherited.letterSpacing
    val textAlign = style.textAlign ?: inherited.textAlign

    val merged = TextStyle(
        color = color?.toComposeColor() ?: ComposeColor.Unspecified,
        fontSize = fontSize?.value?.composeSp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        fontWeight = fontWeight?.let { ComposeFontWeight(it.value) },
        fontFamily = fontFamily?.toComposeFontFamily(),
        lineHeight = lineHeight?.value?.composeSp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        letterSpacing = letterSpacing?.value?.composeSp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        textAlign = textAlign?.toComposeTextAlign() ?: ComposeTextAlign.Unspecified,
    )
    BasicText(
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

private fun FontFamily.toComposeFontFamily(): ComposeFontFamily = when (this) {
    FontFamily.Default -> ComposeFontFamily.Default
    FontFamily.SansSerif -> ComposeFontFamily.SansSerif
    FontFamily.Serif -> ComposeFontFamily.Serif
    FontFamily.Monospace -> ComposeFontFamily.Monospace
}
