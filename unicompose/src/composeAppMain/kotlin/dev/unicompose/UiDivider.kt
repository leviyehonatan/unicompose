package dev.unicompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp as composeDp
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.toComposeColor

@Composable
public actual fun UiDivider(color: Color, thickness: Dp, vertical: Boolean) {
    val base = Modifier.background(color.toComposeColor())
    val sized = if (vertical) {
        base.width(thickness.value.composeDp).fillMaxHeight()
    } else {
        base.height(thickness.value.composeDp).fillMaxWidth()
    }
    Box(modifier = sized)
}
