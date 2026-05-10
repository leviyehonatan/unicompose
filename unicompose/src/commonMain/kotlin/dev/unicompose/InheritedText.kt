package dev.unicompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import dev.unicompose.style.Color
import dev.unicompose.style.FontFamily
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Sp
import dev.unicompose.style.TextAlign

/**
 * Ambient typography properties inherited down the composition tree, mirroring
 * CSS's font-property cascade.
 *
 * When a text-rendering widget (e.g. [UiText]) doesn't specify one of these
 * properties on its own [dev.unicompose.style.Style], it falls back to the
 * value here, which falls back to the parent provider, which finally falls
 * back to the platform default. Set via [ProvideInheritedText] — or use the
 * convenience [ProvideDefaultTextColor] for the color-only case.
 *
 * v0.1 surface covers the inheritable typography slots: color, fontSize,
 * fontWeight, lineHeight, letterSpacing, textAlign. Other [Style] properties
 * (padding, margin, background, border, …) deliberately do *not* inherit —
 * same rule as CSS, where typography cascades but box-model properties don't.
 *
 * Lives in the mechanism layer (`unicompose`). Design libraries on top of
 * `unicompose` use this to thread their typography defaults without every
 * widget having to know about themes.
 */
public data class InheritedText(
    val color: Color? = null,
    val fontSize: Sp? = null,
    val fontWeight: FontWeight? = null,
    val fontFamily: FontFamily? = null,
    val lineHeight: Sp? = null,
    val letterSpacing: Sp? = null,
    val textAlign: TextAlign? = null,
) {
    public companion object {
        /** Empty — no text properties set; everything falls through to defaults. */
        public val Empty: InheritedText = InheritedText()
    }
}

/**
 * The active inherited text properties. Internal — consumers should read via
 * [currentInheritedText] (typed accessor) or write via [ProvideInheritedText]
 * (which merges with the parent rather than replacing).
 */
internal val LocalInheritedText: ProvidableCompositionLocal<InheritedText> =
    compositionLocalOf { InheritedText.Empty }

/** Read the active inherited text properties. */
@Composable
@ReadOnlyComposable
public fun currentInheritedText(): InheritedText = LocalInheritedText.current

/**
 * Read the active default text color. Convenience for
 * `currentInheritedText().color` since color is by far the most commonly
 * read inherited property.
 */
@Composable
@ReadOnlyComposable
public fun currentDefaultTextColor(): Color? = LocalInheritedText.current.color

/**
 * Provide ambient typography properties for descendants. Merges with whatever's
 * already in scope — only non-null fields in [text] override the parent's
 * values, so partial overrides work naturally:
 *
 * ```
 * ProvideInheritedText(InheritedText(color = accent)) {
 *     // descendants get accent color + parent's fontSize/lineHeight/etc.
 * }
 * ```
 */
@Composable
public fun ProvideInheritedText(
    text: InheritedText,
    content: @Composable () -> Unit,
) {
    val parent = currentInheritedText()
    val merged = InheritedText(
        color = text.color ?: parent.color,
        fontSize = text.fontSize ?: parent.fontSize,
        fontWeight = text.fontWeight ?: parent.fontWeight,
        fontFamily = text.fontFamily ?: parent.fontFamily,
        lineHeight = text.lineHeight ?: parent.lineHeight,
        letterSpacing = text.letterSpacing ?: parent.letterSpacing,
        textAlign = text.textAlign ?: parent.textAlign,
    )
    CompositionLocalProvider(LocalInheritedText provides merged, content = content)
}

/**
 * Convenience for providing only a default text color. Equivalent to
 * `ProvideInheritedText(InheritedText(color = color), content)`. The other
 * inherited text properties pass through from the parent unchanged.
 *
 * @param color The color descendants should use when no `Style.color` is set.
 *   Pass `null` to clear (children fall back further up the chain).
 */
@Composable
public fun ProvideDefaultTextColor(
    color: Color?,
    content: @Composable () -> Unit,
) {
    ProvideInheritedText(text = InheritedText(color = color), content = content)
}
