package dev.unicompose

import androidx.compose.runtime.Composable

/**
 * A binary on/off input.
 *
 * Backed by:
 *  - Material 3 `Checkbox` on Compose Multiplatform.
 *  - Native `<input type="checkbox">` on Compose HTML — picks up the user's
 *    OS / browser checkbox styling and supports keyboard focus by default.
 *
 * State is hoisted: the caller owns the [checked] value and is notified via
 * [onCheckedChange]. This keeps the widget pure and testable.
 *
 * Custom visual styling (color, size) is intentionally not exposed in v0.1 —
 * each platform's native rendering wins on accessibility and OS-consistency.
 *
 * @param checked Whether the box is currently checked.
 * @param onCheckedChange Called with the new value after a click or keyboard toggle.
 * @param enabled When false, the box is dimmed and ignores input. Default true.
 *
 * @sample
 * ```
 * var done by remember { mutableStateOf(false) }
 * UiCheckbox(checked = done, onCheckedChange = { done = it })
 * ```
 */
@Composable
public expect fun UiCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
)
