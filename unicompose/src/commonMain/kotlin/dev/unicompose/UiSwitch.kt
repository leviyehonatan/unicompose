package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

/**
 * Two-state on/off toggle.
 *
 * Semantically equivalent to [UiCheckbox] (boolean state), but visually represents
 * an immediate state change — turning a setting on or off — rather than a
 * confirmation/selection. Use [UiCheckbox] for "I have read the terms" and
 * [UiSwitch] for "Wi-Fi: on".
 *
 * Backed by:
 *  - `<input type="checkbox" role="switch">` on Compose HTML — accessibility
 *    tree announces it as a switch.
 *  - Material3 `Switch` on Compose Multiplatform.
 *
 * @param checked Current on/off state.
 * @param onCheckedChange Invoked with the new state when the user toggles.
 * @param enabled Whether the switch responds to interaction.
 * @param style Outer container style. The switch glyph itself is not styled
 *   directly; use [style] for size hints, opacity, margins.
 */
@Composable
public expect fun UiSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    style: Style = Style.Empty,
)
