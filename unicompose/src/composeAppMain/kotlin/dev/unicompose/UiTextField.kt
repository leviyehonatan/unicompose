package dev.unicompose

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

@Composable
public actual fun UiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    enabled: Boolean,
    style: Style,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = style.toModifier(),
        enabled = enabled,
        singleLine = true,
        placeholder = placeholder?.let { hint -> @Composable { Text(hint) } },
    )
}
