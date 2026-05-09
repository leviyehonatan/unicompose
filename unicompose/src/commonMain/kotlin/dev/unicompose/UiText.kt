package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

/**
 * Render a single string of text.
 *
 * Backed by:
 *  - `androidx.compose.material3.Text` on Compose Multiplatform (Android, iOS),
 *  - a semantic `<span>` element on the Compose HTML target.
 *
 * The [Style] properties that apply to text are
 * [color][Style.color], [fontSize][Style.fontSize], [fontWeight][Style.fontWeight],
 * [padding][Style.padding], [margin][Style.margin], [backgroundColor][Style.backgroundColor],
 * [borderRadius][Style.borderRadius], [opacity][Style.opacity], and the size
 * properties [width][Style.width] / [height][Style.height]. Flex-container
 * properties on the same [Style] are ignored — text is a leaf, not a container.
 *
 * For headings (which should render as `<h1>`–`<h3>` for SEO), use the dedicated
 * heading widget when it lands in M2.
 *
 * @param text The string to render. No formatting markup; pass plain text.
 * @param style Visual styling applied to the text. Defaults to [Style.Empty].
 *
 * @sample
 * ```
 * UiText("Hello", style = Style(fontSize = 18.sp, color = Color.Black))
 * ```
 */
@Composable
public expect fun UiText(text: String, style: Style = Style.Empty)
