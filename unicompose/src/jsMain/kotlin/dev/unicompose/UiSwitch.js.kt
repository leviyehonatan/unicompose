package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Input
import org.w3c.dom.HTMLInputElement

@Composable
public actual fun UiSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    style: Style,
) {
    val cls = AtomicCss.classFor(style)
    Input(type = InputType.Checkbox) {
        classes(cls)
        // role=switch tells assistive tech this is a toggle, not a multi-select check.
        attr("role", "switch")
        checked(checked)
        if (!enabled) disabled()
        onChange { event ->
            val target = event.target as? HTMLInputElement ?: return@onChange
            onCheckedChange(target.checked)
        }
    }
}
