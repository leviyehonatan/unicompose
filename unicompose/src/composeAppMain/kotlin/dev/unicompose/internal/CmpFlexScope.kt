package dev.unicompose.internal

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.compositionLocalOf

/**
 * Captured Compose `RowScope` / `ColumnScope` from the enclosing `UiRow` / `UiColumn`,
 * provided so a nested `UiBox` can call the scope-restricted `weight()` modifier
 * even though the modifier is built outside the scope's content lambda.
 *
 * Exactly one of these is non-null at a time; their direction is also reflected in
 * [LocalFlexParent], which is the backend-agnostic carrier of parent layout intent.
 */
internal val LocalRowScope = compositionLocalOf<RowScope?> { null }
internal val LocalColumnScope = compositionLocalOf<ColumnScope?> { null }
