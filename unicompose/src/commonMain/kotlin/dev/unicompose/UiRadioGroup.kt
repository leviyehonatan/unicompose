package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

/**
 * One option in a [UiRadioGroup] — a value and its visible label.
 *
 * @param value The value bound to this option.
 * @param label Visible text shown next to the radio control.
 */
public data class RadioOption<T>(val value: T, val label: String)

/**
 * A vertically stacked group of mutually exclusive options — the user picks
 * exactly one.
 *
 * Backed by:
 *  - A `<fieldset>`-like `<div>` containing one `<label>` per option, each
 *    wrapping an `<input type="radio" name="...">` and the label text. All
 *    inputs share a generated `name` so the browser enforces mutual exclusion
 *    and assistive tech announces the group.
 *  - A `Column` of `Row`s on Compose Multiplatform with a Material3 `RadioButton`
 *    plus a `Text` for each option. Clicking anywhere on the row selects it.
 *
 * The currently-[selected] option is determined by equality on [RadioOption.value],
 * so the value type [T] should have a stable `equals`. For complex types use
 * data classes or a sealed type with a dedicated identity.
 *
 * @param selected Currently selected value. Should match the [RadioOption.value]
 *   of one of the [options]; if no match is found, no radio appears selected.
 * @param options Options in display order.
 * @param onSelectionChange Invoked with the new value when the user picks an option.
 * @param enabled Whether the group responds to interaction.
 * @param style Outer container style (background, padding, gap between rows).
 */
@Composable
public expect fun <T> UiRadioGroup(
    selected: T,
    options: List<RadioOption<T>>,
    onSelectionChange: (T) -> Unit,
    enabled: Boolean = true,
    style: Style = Style.Empty,
)
