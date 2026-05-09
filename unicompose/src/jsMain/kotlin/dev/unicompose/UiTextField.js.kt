package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Input
import org.w3c.dom.HTMLInputElement

@Composable
public actual fun UiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    enabled: Boolean,
    style: Style,
) {
    val cls = AtomicCss.classForInput(style)
    Input(type = InputType.Text) {
        classes(cls)
        value(value)
        placeholder?.let { placeholder(it) }
        if (!enabled) disabled()
        onInput { event ->
            val target = event.target as? HTMLInputElement ?: return@onInput
            onValueChange(target.value)
        }
    }
}
