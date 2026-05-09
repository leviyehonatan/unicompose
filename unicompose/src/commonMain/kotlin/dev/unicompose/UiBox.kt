package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.FlexDirection
import dev.unicompose.style.Style

/**
 * Generic flex container — the layout primitive of unicompose.
 *
 * Defaults to `flex-direction: column` (matching React Native's `<View>` convention,
 * not the web `<div>` default of `display: block`). Override the direction via
 * [Style.flexDirection] or use the convenience wrappers [UiRow] / [UiColumn].
 *
 * Backed by:
 *  - `Column` or `Row` on Compose Multiplatform (selected by [Style.flexDirection]),
 *    with `gap` lowered to `Arrangement.spacedBy`, `justifyContent` to the
 *    main-axis arrangement, and `alignItems` to the cross-axis alignment.
 *  - A `<div>` with `display: flex` on Compose HTML, with the corresponding
 *    CSS properties.
 *
 * **Cross-axis stretch** ([Align.Stretch][dev.unicompose.style.Align.Stretch]) is
 * propagated to children automatically — Compose lacks a native stretch mode so
 * children apply `fillMaxHeight` / `fillMaxWidth` based on the parent's direction.
 *
 * **Flex children** — when this `UiBox` is itself a child of another flex container
 * and has [Style.flex] set, it will receive the appropriate share of the parent's
 * main-axis space (mirroring CSS `flex: N` and Compose's `weight`).
 *
 * @param style Visual and layout styling. Defaults to [Style.Empty].
 * @param content Composable children rendered inside the container.
 *
 * @sample
 * ```
 * UiBox(style = Style(padding = Padding.all(16.dp), gap = 8.dp)) {
 *     UiText("first")
 *     UiText("second")
 * }
 * ```
 */
@Composable
public expect fun UiBox(style: Style = Style.Empty, content: @Composable () -> Unit)
