package dev.unicompose.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import dev.unicompose.InheritedText
import dev.unicompose.ProvideInheritedText
import dev.unicompose.style.FontFamily
import dev.unicompose.style.Sp

/**
 * Provide the active [Tokens] set to all themed `unicompose-base` widgets in
 * [content], plus baseline ambient typography (text color, body fontSize, and
 * a tuned lineHeight) via the mechanism layer's `LocalInheritedText`.
 *
 * Wrap your app's root once (or per-screen if different sections need different
 * themes). Themed widgets read tokens via [currentTokens]; raw `unicompose`
 * primitives ([dev.unicompose.UiText] etc.) inherit typography through the
 * provider we set here. Without this provider, primitives fall back to
 * platform defaults — which differ between Compose Multiplatform's bundled
 * font metrics and a browser's system font, and are visibly jarring on the
 * canvas web bundle.
 *
 * Defaults provided:
 *  - color: `tokens.colors.textPrimary`
 *  - fontSize: `tokens.type.md` (15sp by default)
 *  - lineHeight: `fontSize * 1.4` — a sensible body-text ratio that matches
 *    typical browser defaults
 *
 * Heading widgets ([dev.unicompose.UiHeading], [Heading], [H1]/[H2]/[H3])
 * override these via their own `defaultHeadingStyle`, so headings still get
 * their level-specific size/weight.
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
    val bodyFontSize = tokens.type.md
    val bodyLineHeight = Sp(bodyFontSize.value * 1.4f)
    // On the web, lower the active token set to CSS custom properties so any
    // `var(--uc-...)` reference in the linked unicompose-generated.css (or in
    // the runtime <style>) resolves to the right value. No-op on CMP.
    SideEffect { applyThemeCssVariables(tokens) }
    CompositionLocalProvider(LocalTokens provides tokens) {
        ProvideInheritedText(
            text = InheritedText(
                color = tokens.colors.textPrimary,
                fontSize = bodyFontSize,
                fontFamily = FontFamily.Default,
                lineHeight = bodyLineHeight,
            ),
            content = content,
        )
    }
}
