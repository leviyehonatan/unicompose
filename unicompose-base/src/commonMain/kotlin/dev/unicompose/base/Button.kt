package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.ProvideDefaultTextColor
import dev.unicompose.UiButton
import dev.unicompose.UiText
import dev.unicompose.style.Border
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.Color
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Padding
import dev.unicompose.style.Style
import dev.unicompose.style.dp

/**
 * Visual variant of a themed [Button]. Each maps to a different color/border
 * recipe pulled from the active [Tokens].
 *
 *  - [Primary]: filled with `colors.accent`, label in `colors.onAccent`. The
 *    default call-to-action shape.
 *  - [Secondary]: outlined with `colors.borderSubtle`, label in `colors.textPrimary`.
 *    Lower-emphasis action.
 *  - [Ghost]: no background or border, label in `colors.accent`. Minimal-weight
 *    action — useful in dense UIs and for tertiary verbs.
 */
public enum class ButtonVariant { Primary, Secondary, Ghost }

/**
 * A themed button with a string label.
 *
 * Wraps the unstyled [UiButton] primitive with token-driven defaults keyed off
 * [variant]. Style overrides layer on top of [ButtonDefaults.style]. The label
 * inherits text color from the variant unless overridden.
 *
 * For buttons with mixed inline content (icon + text, multiple text spans),
 * use the content-lambda overload below.
 *
 * @sample
 * ```
 * Button(onClick = save, text = "Save")
 * Button(onClick = cancel, text = "Cancel", variant = ButtonVariant.Secondary)
 * ```
 */
@Composable
public fun Button(
    onClick: () -> Unit,
    text: String,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    style: Style = Style.Empty,
) {
    Button(onClick = onClick, variant = variant, enabled = enabled, style = style) {
        UiText(text)
    }
}

/**
 * A themed button with arbitrary inline content (Kobweb-style).
 *
 * Same defaults and variant catalog as the string-label overload, but the label
 * is a Composable lambda — useful for icon + text combinations or any other
 * inline content. The button's resolved label color is propagated to descendants
 * via [ProvideDefaultTextColor], so a bare `Text("...")` in [content] picks it
 * up automatically.
 *
 * @param onClick Invoked on click / tap / Enter.
 * @param variant Visual recipe — see [ButtonVariant]. Default is [ButtonVariant.Primary].
 * @param enabled When false, the button is dimmed and ignores input.
 * @param style Style overrides layered on top of [ButtonDefaults.style].
 * @param content Button label content. Typically `Text("...")`; can include icons.
 *
 * @sample
 * ```
 * Button(onClick = save) { Text("Save") }
 * Button(onClick = retry, variant = ButtonVariant.Secondary) {
 *     UiIcon(...)
 *     Text("Retry")
 * }
 * ```
 */
@Composable
public fun Button(
    onClick: () -> Unit,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    style: Style = Style.Empty,
    content: @Composable () -> Unit,
) {
    val resolved = ButtonDefaults.style(variant) + style
    UiButton(onClick = onClick, style = resolved, enabled = enabled) {
        ProvideDefaultTextColor(resolved.color, content)
    }
}

/** Default style recipes for [Button] variants. */
public object ButtonDefaults {
    /**
     * Returns the token-resolved style for a given [variant]. Includes color,
     * background/border, padding, corner radius, and label typography.
     */
    @Composable
    public fun style(variant: ButtonVariant): Style {
        val t = currentTokens()
        val shared = Style(
            padding = Padding.symmetric(vertical = t.space.sm, horizontal = t.space.md),
            borderRadius = BorderRadius.all(t.radii.md),
            fontSize = t.type.sm,
            fontWeight = FontWeight.Medium,
        )
        return when (variant) {
            ButtonVariant.Primary -> shared + Style(
                backgroundColor = t.colors.accent,
                color = t.colors.onAccent,
            )
            ButtonVariant.Secondary -> shared + Style(
                backgroundColor = Color.Transparent,
                color = t.colors.textPrimary,
                border = Border.all(width = 1.dp, color = t.colors.borderSubtle),
            )
            ButtonVariant.Ghost -> shared + Style(
                backgroundColor = Color.Transparent,
                color = t.colors.accent,
            )
        }
    }
}
