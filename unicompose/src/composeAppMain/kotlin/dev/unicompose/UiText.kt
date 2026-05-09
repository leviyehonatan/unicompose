package dev.unicompose

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight as ComposeFontWeight
import androidx.compose.ui.unit.sp as composeSp
import dev.unicompose.style.Style
import dev.unicompose.style.toComposeColor
import dev.unicompose.style.toModifier

@Composable
public actual fun UiText(text: String, style: Style) {
    val base = LocalTextStyle.current
    val merged = base.copy(
        color = style.color?.toComposeColor() ?: ComposeColor.Unspecified,
        fontSize = style.fontSize?.value?.composeSp ?: base.fontSize,
        fontWeight = style.fontWeight?.let { ComposeFontWeight(it.value) } ?: base.fontWeight,
    )
    Text(
        text = text,
        modifier = style.toModifier(),
        style = merged,
    )
}
