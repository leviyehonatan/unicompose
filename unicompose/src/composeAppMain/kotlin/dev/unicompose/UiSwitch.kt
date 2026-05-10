package dev.unicompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

// Browser-default-shaped primitive switch. Rounded-pill track + circle thumb;
// no animation (post-v0.1). Off = gray track, on = accent fill.
private val TrackWidth = 32.dp
private val TrackHeight = 18.dp
private val ThumbSize = 14.dp
private val ThumbInset = 2.dp
private val OffTrack = Color(0xFFC7C8CC)
private val OnTrack = Color(0xFF2F7DEC)
private val ThumbColor = Color.White

@Composable
public actual fun UiSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    style: Style,
) {
    val trackShape = RoundedCornerShape(TrackHeight / 2)
    val thumbX = if (checked) TrackWidth - ThumbSize - ThumbInset else ThumbInset
    Box(
        modifier = style.toModifier()
            .size(TrackWidth, TrackHeight)
            .let { if (enabled) it else it.alpha(0.5f) }
            .toggleable(
                value = checked,
                role = Role.Switch,
                enabled = enabled,
                onValueChange = onCheckedChange,
            )
            .background(if (checked) OnTrack else OffTrack, trackShape),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbX, y = ThumbInset)
                .size(ThumbSize)
                .background(ThumbColor, CircleShape),
        )
    }
}
