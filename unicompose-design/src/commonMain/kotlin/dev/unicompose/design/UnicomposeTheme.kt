package dev.unicompose.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.unicompose.LocalDefaultTextColor

/**
 * Provide the active [Tokens] set to all themed `unicompose-design` widgets in
 * [content], and propagate `tokens.colors.textPrimary` as the ambient text color
 * via the underlying mechanism's [LocalDefaultTextColor].
 *
 * Wrap your app's root once (or per-screen if different sections need different
 * themes). Themed widgets read tokens via [currentTokens]; raw `unicompose`
 * primitives ([dev.unicompose.UiText] etc.) inherit text color through the
 * `LocalDefaultTextColor` we set here.
 *
 * Apps that don't wrap in this still work — primitives fall back to platform
 * defaults — but they don't get themed colors.
 *
 * @param tokens The token set to make available beneath this provider.
 *   Defaults to [LightTokens].
 * @param content App content rendered with [tokens] in scope.
 *
 * @sample
 * ```
 * UnicomposeTheme(tokens = if (darkMode) DarkTokens else LightTokens) {
 *     App()
 * }
 * ```
 */
@Composable
public fun UnicomposeTheme(
    tokens: Tokens = LightTokens,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalTokens provides tokens,
        LocalDefaultTextColor provides tokens.colors.textPrimary,
        content = content,
    )
}
