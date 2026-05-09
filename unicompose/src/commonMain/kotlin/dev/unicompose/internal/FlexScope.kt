package dev.unicompose.internal

import androidx.compose.runtime.compositionLocalOf
import dev.unicompose.style.FlexDirection

/**
 * Backend-agnostic information about the immediate flex parent, threaded through
 * composition so child widgets can adapt their rendering to parent layout intent.
 *
 * Used to bridge two cross-cutting CSS-flex concerns that need parent context:
 *  - `Style.flex` (CSS `flex: N`) — only applies when the element is a flex child;
 *    on Compose Multiplatform this becomes `RowScope.weight` / `ColumnScope.weight`,
 *    which require the *enclosing* Row/Column scope.
 *  - `alignItems = Stretch` on a parent — on web this is just CSS, but on CMP
 *    `Row`/`Column` have no built-in cross-axis stretch and must be implemented
 *    by each child applying `fillMaxHeight` / `fillMaxWidth`.
 */
internal data class FlexParent(
    val direction: FlexDirection,
    val stretchChildren: Boolean,
)

internal val LocalFlexParent = compositionLocalOf<FlexParent?> { null }
