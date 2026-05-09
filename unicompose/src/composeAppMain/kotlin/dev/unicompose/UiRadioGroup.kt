package dev.unicompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp as composeDp
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

@Composable
public actual fun <T> UiRadioGroup(
    selected: T,
    options: List<RadioOption<T>>,
    onSelectionChange: (T) -> Unit,
    enabled: Boolean,
    style: Style,
) {
    Column(
        modifier = style.toModifier(),
        verticalArrangement = Arrangement.spacedBy(4.composeDp),
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            Row(
                modifier = Modifier.selectable(
                    selected = isSelected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = { onSelectionChange(option.value) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null, // handled by Row.selectable
                    enabled = enabled,
                )
                Text(option.label)
            }
        }
    }
}
