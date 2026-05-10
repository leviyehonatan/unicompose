package dev.unicompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

// Browser-default-shaped primitive checkbox. Mirrors the DOM `<input type="checkbox">`
// on the JS backend: a small bordered square that fills with an accent color and
// shows a white checkmark when checked. Deliberately NOT Material — the mechanism
// layer is meant to be the unstyled UA-equivalent on every platform; the design
// system (`unicompose-base`) layers themed variants on top via wrappers.
private val Size = 14.dp
private val CornerRadius = 2.dp
private val BorderColor = Color(0xFF8A8D91)
private val FillColor = Color(0xFF2F7DEC)
private val CheckColor = Color.White

@Composable
public actual fun UiCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    val shape = RoundedCornerShape(CornerRadius)
    val chrome = if (checked) {
        Modifier.background(FillColor, shape)
    } else {
        Modifier.background(Color.White, shape).border(1.dp, BorderColor, shape)
    }
    Box(
        modifier = Modifier
            .size(Size)
            .let { if (enabled) it else it.alpha(0.5f) }
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                enabled = enabled,
                onValueChange = onCheckedChange,
            )
            .then(chrome),
    ) {
        if (checked) {
            Canvas(Modifier.size(Size)) {
                val w = size.width
                val path = Path().apply {
                    moveTo(w * 0.22f, w * 0.52f)
                    lineTo(w * 0.43f, w * 0.72f)
                    lineTo(w * 0.78f, w * 0.30f)
                }
                drawPath(
                    path = path,
                    color = CheckColor,
                    style = Stroke(width = w * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}
