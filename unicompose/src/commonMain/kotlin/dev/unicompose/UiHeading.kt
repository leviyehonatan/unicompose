package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

/**
 * Hierarchy level for [UiHeading]. Maps to semantic HTML elements on web and to
 * size/weight defaults on Compose Multiplatform.
 *
 * Use exactly one [H1] per page, then nest [H2] under it, [H3] under [H2], etc.
 * Search engines and screen readers rely on this structure.
 */
public enum class HeadingLevel { H1, H2, H3 }

/**
 * A semantic heading.
 *
 * Backed by:
 *  - The matching `<h1>` / `<h2>` / `<h3>` element on Compose HTML — emitted as
 *    real HTML so search engines and screen readers see the document outline.
 *  - A `Text` with default size and weight on Compose Multiplatform.
 *
 * The default visual style per level (see [defaultHeadingStyle]) can be overridden
 * by passing values on [style]; user-provided values take precedence over the
 * level defaults.
 *
 * @param level The semantic level — see [HeadingLevel].
 * @param text The heading content.
 * @param style Visual overrides. Merged on top of the level's default style.
 *
 * @sample
 * ```
 * UiHeading(HeadingLevel.H1, "unicompose")
 * UiHeading(HeadingLevel.H2, "Quick start", style = Style(color = Subtle))
 * ```
 */
@Composable
public expect fun UiHeading(
    level: HeadingLevel,
    text: String,
    style: Style = Style.Empty,
)
