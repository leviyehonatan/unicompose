package dev.unicompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.unicompose.style.AtomicCss
import dev.unicompose.style.Style
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

@Composable
public actual fun <T> UiRadioGroup(
    selected: T,
    options: List<RadioOption<T>>,
    onSelectionChange: (T) -> Unit,
    enabled: Boolean,
    style: Style,
) {
    val cls = AtomicCss.classFor(style)
    // A unique name groups the radios for the browser's mutual-exclusion logic
    // and for assistive tech's group announcement. Recomputed per call site
    // (per RadioGroup composition) — remember keeps it stable across recompositions.
    val groupName = remember { "uc-rg-${nextRadioGroupId()}" }
    Div(attrs = {
        classes(cls)
        attr("role", "radiogroup")
    }) {
        options.forEach { option ->
            Label(attrs = {
                style {
                    property("display", "flex")
                    property("align-items", "center")
                    property("gap", "8px")
                    property("padding", "4px 0")
                    property("cursor", if (enabled) "pointer" else "not-allowed")
                }
            }) {
                Input(type = InputType.Radio) {
                    name(groupName)
                    checked(option.value == selected)
                    if (!enabled) disabled()
                    onChange { onSelectionChange(option.value) }
                }
                Text(option.label)
            }
        }
    }
}

private var radioGroupCounter = 0
private fun nextRadioGroupId(): Int = ++radioGroupCounter
