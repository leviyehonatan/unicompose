package dev.unicompose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Input
import org.w3c.dom.HTMLInputElement

@Composable
public actual fun UiCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    Input(type = InputType.Checkbox) {
        checked(checked)
        if (!enabled) disabled()
        onChange { event ->
            val target = event.target as? HTMLInputElement ?: return@onChange
            onCheckedChange(target.checked)
        }
    }
}
