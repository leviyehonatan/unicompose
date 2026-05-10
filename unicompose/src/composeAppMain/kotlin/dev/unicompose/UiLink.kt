package dev.unicompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight as ComposeFontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp as composeSp
import dev.unicompose.style.Style
import dev.unicompose.style.toComposeColor
import dev.unicompose.style.toModifier

@Composable
public actual fun UiLink(text: String, href: String, style: Style) {
    val uriHandler = LocalUriHandler.current
    val merged = TextStyle(
        color = style.color?.toComposeColor() ?: ComposeColor.Unspecified,
        fontSize = style.fontSize?.value?.composeSp ?: TextUnit.Unspecified,
        fontWeight = style.fontWeight?.let { ComposeFontWeight(it.value) },
        textDecoration = TextDecoration.Underline,
    )
    BasicText(
        text = text,
        modifier = style.toModifier().clickable {
            runCatching { uriHandler.openUri(href) }
        },
        style = merged,
    )
}
