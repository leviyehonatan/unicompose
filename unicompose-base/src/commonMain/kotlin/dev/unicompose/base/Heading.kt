package dev.unicompose.base

import androidx.compose.runtime.Composable
import dev.unicompose.HeadingLevel
import dev.unicompose.UiHeading
import dev.unicompose.UiText
import dev.unicompose.style.Style

/**
 * A semantic heading themed by the active [Tokens].
 *
 * Wraps the underlying [UiHeading] primitive with default text color drawn from
 * `colors.textPrimary`. The level's intrinsic size and weight (set by `unicompose`'s
 * `defaultHeadingStyle`) carry over; [style] overrides layer on top of both.
 *
 * Lives in `unicompose-base` because the color choice is a design opinion. The
 * underlying `UiHeading` in `unicompose` keeps the system text color so the
 * primitive remains usable without a theme.
 *
 * For more HTML-like call sites, prefer the [H1] / [H2] / [H3] shortcuts —
 * they're thin aliases that call this function with the matching level.
 *
 * @param level The semantic level — see [HeadingLevel] in unicompose.
 * @param text The heading content.
 * @param style Visual overrides layered on top of token defaults.
 */
@Composable
public fun Heading(
    level: HeadingLevel,
    text: String,
    style: Style = Style.Empty,
) {
    UiHeading(level = level, text = text, style = (HeadingStyle + style).resolveRefs(currentTokens()))
}

/**
 * Default styling layered under user-provided overrides in [Heading]. Top-
 * level val so the IR plugin extracts it: emits a single `.uc-{hash}` class
 * with `color: var(--uc-colors-textPrimary)`. Size + weight per heading
 * level still come from `unicompose`'s `defaultHeadingStyle`.
 */
public val HeadingStyle: Style = Style(
    colorRef = TokenRefs.colors.textPrimary,
)

/** HTML-aligned shortcut for `Heading(HeadingLevel.H1, text, style)`. */
@Composable
public fun H1(text: String, style: Style = Style.Empty): Unit =
    Heading(HeadingLevel.H1, text, style)

/** HTML-aligned shortcut for `Heading(HeadingLevel.H2, text, style)`. */
@Composable
public fun H2(text: String, style: Style = Style.Empty): Unit =
    Heading(HeadingLevel.H2, text, style)

/** HTML-aligned shortcut for `Heading(HeadingLevel.H3, text, style)`. */
@Composable
public fun H3(text: String, style: Style = Style.Empty): Unit =
    Heading(HeadingLevel.H3, text, style)

/** Default style helpers for [Heading] / [H1] / [H2] / [H3]. */
public object HeadingDefaults {
    /** Backwards-compatible accessor — returns the same [HeadingStyle] constant. */
    @Composable
    public fun style(): Style = HeadingStyle
}

/**
 * Plain inline text — an alias over [dev.unicompose.UiText] for natural reading
 * inside content lambdas like `Button(onClick = ...) { Text("Save") }`. Saves a
 * `dev.unicompose` import jump on the call site.
 *
 * @param text The text to render.
 * @param style Visual styling, layered on top of any inherited text properties.
 */
@Composable
public fun Text(text: String, style: Style = Style.Empty): Unit =
    UiText(text = text, style = style)
