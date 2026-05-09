package dev.unicompose.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * The active design tokens for the current composition subtree.
 *
 * Defaults to [LightTokens] when no [UnicomposeTheme] provider is in scope, so
 * widgets work without explicit setup. Read it directly when you need raw access;
 * in most cases prefer [currentTokens].
 */
public val LocalTokens: ProvidableCompositionLocal<Tokens> =
    compositionLocalOf { LightTokens }

/**
 * Read the active token set. Equivalent to `LocalTokens.current` but reads more
 * naturally inside widget defaults: `currentTokens().colors.accent`.
 */
@Composable
@ReadOnlyComposable
public fun currentTokens(): Tokens = LocalTokens.current
