package dev.unicompose.base

import androidx.compose.runtime.Composable
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
 * A themed button.
 *
 * Wraps the unstyled [UiButton] primitive with token-driven default colors,
 * padding, corner radius, and label typography keyed off [variant]. Pass
 * [style] to override individual properties; the label inherits text color
 * from the variant unless overridden.
 *
 * Lives in `unicompose-base` because the variant catalog and color choices
 * are design opinions. The underlying `UiButton` stays unstyled in
 * `unicompose` so other design libraries can layer their own button systems.
 *
 * @param onClick Invoked on click / tap / Enter.
 * @param text Label text.
 * @param variant Visual recipe — see [ButtonVariant]. Default is [ButtonVariant.Primary].
 * @param enabled When false, the button is dimmed and ignores input.
 * @param style Style overrides layered on top of [ButtonDefaults.style].
 *
 * @sample
 * ```
 * Button(onClick = {}, text = "Save")
 * Button(onClick = {}, text = "Cancel", variant = ButtonVariant.Secondary)
 * Button(onClick = {}, text = "Learn more", variant = ButtonVariant.Ghost)
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
    val resolved = ButtonDefaults.style(variant) + style
    UiButton(onClick = onClick, style = resolved, enabled = enabled) {
        UiText(
            text,
            style = Style(
                color = resolved.color,
                fontSize = resolved.fontSize,
                fontWeight = resolved.fontWeight,
            ),
        )
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
