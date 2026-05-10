package dev.unicompose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

// Browser-default-shaped primitive text field. Mirrors the DOM `<input type="text">`
// on the JS backend: a thin-bordered, lightly-padded single-line input with a
// placeholder overlay that disappears on first keystroke. Deliberately NOT
// Material's OutlinedTextField — the mechanism layer is the unstyled UA
// equivalent; design-system styling layers on top via wrappers.
private val BorderColor = Color(0xFF8A8D91)
private val TextColor = Color(0xFF1F2328)
private val PlaceholderColor = Color(0xFF8A8D91)
private val Background = Color.White
private val FieldShape = RoundedCornerShape(4.dp)

@Composable
public actual fun UiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    enabled: Boolean,
    style: Style,
) {
    val textStyle = TextStyle(color = TextColor, fontSize = 14.sp)
    val placeholderStyle = textStyle.copy(color = PlaceholderColor)
    Box(
        modifier = style.toModifier()
            .background(Background, FieldShape)
            .border(1.dp, BorderColor, FieldShape)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .let { if (enabled) it else it.alpha(0.5f) },
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(TextColor),
            modifier = Modifier,
        )
        if (value.isEmpty() && placeholder != null) {
            BasicText(text = placeholder, style = placeholderStyle)
        }
    }
}
