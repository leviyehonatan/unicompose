package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

/**
 * A single-line text input.
 *
 * Backed by:
 *  - Material 3 `OutlinedTextField` on Compose Multiplatform.
 *  - Native `<input type="text">` on Compose HTML, with browser-default styling
 *    reset so [style] is the only source of visual properties.
 *
 * State is hoisted: the caller owns the [value] and is notified via [onValueChange]
 * on every keystroke. This keeps the widget pure and easy to test.
 *
 * Multi-line input is not supported in v0.1 — a separate `UiTextArea` will be
 * added when needed.
 *
 * @param value Current text content.
 * @param onValueChange Called with the new content on every change.
 * @param placeholder Hint text shown when [value] is empty. Optional.
 * @param enabled When false, the field is dimmed and rejects input. Default true.
 * @param style Visual styling. Defaults to [Style.Empty].
 *
 * @sample
 * ```
 * var name by remember { mutableStateOf("") }
 * UiTextField(
 *     value = name,
 *     onValueChange = { name = it },
 *     placeholder = "Your name",
 * )
 * ```
 */
@Composable
public expect fun UiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    enabled: Boolean = true,
    style: Style = Style.Empty,
)
