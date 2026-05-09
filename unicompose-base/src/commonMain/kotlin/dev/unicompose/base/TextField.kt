package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.UiColumn
import dev.unicompose.UiText
import dev.unicompose.UiTextField
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Style

/**
 * A themed single-line text input with optional label.
 *
 * Wraps the unstyled [UiTextField] primitive with a label rendered above (when
 * provided) and themed colors. State is hoisted exactly like the underlying
 * primitive — the caller owns [value] and gets [onValueChange] callbacks.
 *
 * Lives in `unicompose-base` because the label-above-input layout and the
 * label/text typography choices are design opinions. The underlying
 * `UiTextField` in `unicompose` keeps the platform-native input styling so
 * the primitive remains usable without a theme.
 *
 * v0.1 doesn't yet style the input chrome itself (border, focus ring) — both
 * Compose Multiplatform and Compose HTML render their respective platform
 * defaults. Token-driven input styling is a follow-up once the underlying
 * primitive accepts richer style hooks.
 *
 * @param value Current text content.
 * @param onValueChange Called with the new text on every keystroke.
 * @param label Optional label rendered above the input. Pass `null` to omit.
 * @param placeholder Hint text shown when [value] is empty.
 * @param enabled When false, the field is disabled and read-only.
 *
 * @sample
 * ```
 * var name by remember { mutableStateOf("") }
 * TextField(
 *     value = name,
 *     onValueChange = { name = it },
 *     label = "Name",
 *     placeholder = "Your name",
 * )
 * ```
 */
@Composable
public fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
) {
    val t = currentTokens()
    UiColumn(style = Style(gap = t.space.xs)) {
        if (label != null) {
            UiText(
                label,
                style = Style(
                    color = t.colors.textSecondary,
                    fontSize = t.type.xs,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        UiTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            enabled = enabled,
        )
    }
}
