package dev.unicompose

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable

@Composable
public actual fun UiCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
    )
}
