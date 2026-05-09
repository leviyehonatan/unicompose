package dev.unicompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

@Composable
public actual fun UiButton(
    onClick: () -> Unit,
    style: Style,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val base = style.toModifier()
        .semantics { role = Role.Button }
        .let { if (enabled) it.clickable(onClick = onClick) else it.alpha(0.5f) }
    Box(modifier = base) {
        content()
    }
}
