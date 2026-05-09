package dev.unicompose

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

@Composable
public actual fun UiSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    style: Style,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = style.toModifier(),
        enabled = enabled,
    )
}
