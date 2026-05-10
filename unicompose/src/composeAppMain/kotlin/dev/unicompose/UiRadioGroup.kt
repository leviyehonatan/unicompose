package dev.unicompose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp as composeDp
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

// Browser-default-shaped primitive radio group: circle border, filled inner
// circle when selected, paired with a label per option.
private val OuterSize = 14.composeDp
private val InnerSize = 7.composeDp
private val BorderColor = Color(0xFF8A8D91)
private val FillColor = Color(0xFF2F7DEC)

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
                modifier = Modifier
                    .let { if (enabled) it else it.alpha(0.5f) }
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelectionChange(option.value) },
                    )
                    .padding(vertical = 4.composeDp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(OuterSize)
                        .border(1.composeDp, BorderColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Box(modifier = Modifier.size(InnerSize).background(FillColor, CircleShape))
                    }
                }
                BasicText(
                    text = option.label,
                    modifier = Modifier.padding(start = 8.composeDp),
                    style = TextStyle(color = Color(0xFF1F2328)),
                )
            }
        }
    }
}
