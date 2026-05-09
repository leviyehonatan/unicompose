package dev.unicompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import dev.unicompose.style.Color

/**
 * Ambient text color, inherited down the composition tree like CSS `color`.
 *
 * `UiText` (and any other text-rendering primitive) reads this if its own
 * `Style.color` is null, falling back to the platform default if the local is
 * also null. Set it via [ProvideDefaultTextColor] (or `CompositionLocalProvider`
 * directly) to provide a theme-aware text color from above.
 *
 * Lives in the mechanism layer (unicompose) — design libraries on top of
 * unicompose use this to thread their text color choice without primitives
 * having to know about themes.
 */
public val LocalDefaultTextColor: ProvidableCompositionLocal<Color?> =
    compositionLocalOf { null }

/**
 * Read the active default text color, or `null` if none is provided. `UiText`
 * widgets call this internally; user widgets that render text should also call
 * it to participate in inheritance.
 */
@Composable
@ReadOnlyComposable
public fun currentDefaultTextColor(): Color? = LocalDefaultTextColor.current

/**
 * Provide a default text color to all descendant unicompose widgets. Convenience
 * over `CompositionLocalProvider(LocalDefaultTextColor provides color) { ... }`.
 *
 * @param color The color descendants should use when no `Style.color` is set.
 *   Pass `null` to clear (children fall back to platform default).
 */
@Composable
public fun ProvideDefaultTextColor(
    color: Color?,
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDefaultTextColor provides color,
        content = content,
    )
}
