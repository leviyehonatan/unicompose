package dev.unicompose.design

import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.Sp
import dev.unicompose.style.argb
import dev.unicompose.style.dp
import dev.unicompose.style.rgb
import dev.unicompose.style.sp

/**
 * Design-token surface for the unicompose-design system — a small, opinionated set
 * of named values that themed widgets read instead of baking literals.
 *
 * Tokens are the only hook for theming, light/dark mode, and brand customization
 * within `unicompose-design`. Wrap your app in `UnicomposeTheme(tokens = MyTokens)`
 * and every themed widget reads from the active set.
 *
 * The surface is deliberately narrow in v0.1 — ten color slots, a five-step
 * spacing scale, a five-step type scale, three corner radii. Apps with richer
 * needs layer their own typed token shape on top and convert at the boundary.
 *
 * Note: tokens live in `unicompose-design`, not in the underlying `unicompose`
 * mechanism. The mechanism (Style + atomic CSS + flex) has no opinion about
 * design tokens — different design libraries can ship their own token shapes.
 *
 * @property colors Foreground / background / accent / status color slots.
 * @property space  T-shirt-sized spacing scale used by widget defaults.
 * @property type   T-shirt-sized typography scale.
 * @property radii  Corner-radius scale used by widget defaults.
 */
public data class Tokens(
    val colors: ColorTokens,
    val space: SpaceTokens,
    val type: TypeTokens,
    val radii: RadiusTokens,
)

/**
 * Color tokens.
 *
 * Names are role-based (intent), not appearance-based — `accent`, not `blue` —
 * so light and dark token sets can swap underlying RGB without renaming.
 *
 * @property accent          Primary brand / call-to-action color.
 * @property onAccent        Foreground text/icon color drawn on top of `accent`.
 * @property bgPage          Background of the overall page / root container.
 * @property bgSurface       Background of cards, inputs, and elevated surfaces.
 * @property bgSubtle        Slightly tinted fill for badges, chips, hover states —
 *                           neither pure surface nor a divider.
 * @property textPrimary     Default text color for body content.
 * @property textSecondary   Subdued text color for captions, hints, secondary labels.
 * @property borderSubtle    Color of dividers, hairlines, and subtle borders.
 * @property error           Destructive / error state color.
 * @property success         Success / positive state color.
 */
public data class ColorTokens(
    val accent: Color,
    val onAccent: Color,
    val bgPage: Color,
    val bgSurface: Color,
    val bgSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val borderSubtle: Color,
    val error: Color,
    val success: Color,
)

/**
 * Spacing tokens — t-shirt-sized scale used for padding, margin, and gap defaults.
 *
 * Sizes are deliberately not arithmetic (no `xs * 2 = sm`) — designers tune the
 * scale by feel, and forcing a strict ratio yields awkward intermediate sizes.
 */
public data class SpaceTokens(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
)

/**
 * Typography size tokens — t-shirt-sized scale used by body text defaults.
 *
 * Heading sizes are not driven by this scale; they live in widget defaults so
 * they can scale independently of body text.
 */
public data class TypeTokens(
    val xs: Sp,
    val sm: Sp,
    val md: Sp,
    val lg: Sp,
    val xl: Sp,
)

/**
 * Corner-radius tokens. Three steps cover most needs (small chips, medium cards,
 * large rounded panels); per-corner radii will arrive when the underlying Style
 * supports them.
 */
public data class RadiusTokens(
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
)

/** Default light-mode tokens. The fallback when no explicit theme is provided. */
public val LightTokens: Tokens = Tokens(
    colors = ColorTokens(
        accent = rgb(0x35, 0x6D, 0xF5),
        onAccent = Color.White,
        bgPage = rgb(0xF7, 0xF7, 0xF8),
        bgSurface = Color.White,
        bgSubtle = rgb(0xEE, 0xEF, 0xF2),
        textPrimary = rgb(0x14, 0x14, 0x14),
        textSecondary = rgb(0x6B, 0x6F, 0x76),
        borderSubtle = argb(31, 0, 0, 0),
        error = rgb(0xD5, 0x3F, 0x3F),
        success = rgb(0x2E, 0x8B, 0x57),
    ),
    space = SpaceTokens(xs = 4.dp, sm = 8.dp, md = 16.dp, lg = 24.dp, xl = 32.dp),
    type = TypeTokens(xs = 11.sp, sm = 13.sp, md = 15.sp, lg = 18.sp, xl = 22.sp),
    radii = RadiusTokens(sm = 4.dp, md = 8.dp, lg = 12.dp),
)

/** Default dark-mode tokens. Spacing/type/radius scales match [LightTokens]. */
public val DarkTokens: Tokens = Tokens(
    colors = ColorTokens(
        accent = rgb(0x5A, 0x8A, 0xFF),
        onAccent = rgb(0x0A, 0x0A, 0x0A),
        bgPage = rgb(0x12, 0x13, 0x16),
        bgSurface = rgb(0x1B, 0x1C, 0x1F),
        bgSubtle = rgb(0x25, 0x27, 0x2C),
        textPrimary = rgb(0xEC, 0xED, 0xEF),
        textSecondary = rgb(0x9A, 0x9E, 0xA5),
        borderSubtle = argb(40, 255, 255, 255),
        error = rgb(0xE6, 0x5C, 0x5C),
        success = rgb(0x49, 0xC8, 0x80),
    ),
    space = LightTokens.space,
    type = LightTokens.type,
    radii = LightTokens.radii,
)
