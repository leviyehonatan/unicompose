package dev.unicompose.style

import kotlin.jvm.JvmInline

/**
 * The styling primitive of unicompose — an immutable, typed description of how a
 * widget should look and lay out its children.
 *
 * Each property maps to an equivalent on both backends:
 *  - On Compose Multiplatform (Android / iOS) it reduces to a Compose `Modifier`
 *    chain combined with `Row` / `Column` arrangement and alignment arguments.
 *  - On Compose HTML it compiles to an atomic CSS class registered into a
 *    singleton `<style>` element.
 *
 * The property surface is deliberately narrow: only properties whose semantics map
 * cleanly to *both* backends are exposed. For platform-specific styling that does
 * not generalize, drop down to a backend's native API.
 *
 * Construct styles by named arguments and combine them with [plus]:
 * ```
 * val card = Style(padding = Padding.all(16.dp), backgroundColor = Color.White)
 * val emphasized = card + Style(borderRadius = BorderRadius.all(12.dp))
 * ```
 *
 * @property padding Inside-edge spacing, applied via Compose's `Modifier.padding` / CSS `padding`.
 * @property margin Outside-edge spacing. On Compose Multiplatform this is implemented
 *   by an outer wrapping `Box` since Compose lacks a child-level margin modifier.
 * @property backgroundColor Solid fill behind the element's content. When
 *   [backgroundGradient] is also set, the gradient paints on top of this color
 *   (matches CSS: solid color first, gradient image on top).
 * @property backgroundGradient Linear gradient fill behind the element's content,
 *   above [backgroundColor] if both are set. See [LinearGradient].
 * @property color Foreground color used for text by default.
 * @property fontSize Text size in scale-independent pixels.
 * @property fontWeight Text weight — see [FontWeight] for supported values.
 * @property fontFamily Font family — see [FontFamily] for supported values.
 *   Note: exact font appearance differs per backend (web DOM uses browser system
 *   fonts; CMP / canvas web uses Skia's bundled fonts). The enum chooses a
 *   semantic family; the platform picks the actual face.
 * @property lineHeight Line height for text content. Treated as an absolute size
 *   (Sp on CMP, px on web), not a CSS unitless multiplier.
 * @property letterSpacing Tracking applied between glyphs in text content.
 * @property textAlign Horizontal alignment of text content within its line box.
 * @property borderRadius Corner radius — uniform via [BorderRadius.all] or per-corner
 *   via [BorderRadius.Companion.invoke]. Affects element shape and clipping.
 * @property border Solid border around the element. v0.1 supports uniform borders only;
 *   per-side borders (different colors/widths per edge) are deferred — Compose's
 *   `Modifier.border` is uniform and per-side requires custom drawing.
 * @property boxShadow Drop shadow behind the element. Visually equivalent across
 *   backends but not pixel-equivalent — see [Shadow] for the cross-platform caveat
 *   about offset.
 * @property flexDirection Layout axis for child widgets in a [dev.unicompose.UiBox].
 *   Defaults to [FlexDirection.Column] (matching React Native), not the CSS default of `row`.
 * @property alignItems Cross-axis alignment of children in a flex container.
 * @property justifyContent Main-axis arrangement of children in a flex container.
 * @property gap Spacing between adjacent children in a flex container.
 * @property width Element width — see [Size] for supported sizing modes.
 * @property height Element height — see [Size] for supported sizing modes.
 * @property flex Flex grow factor for this element when it is a child of a flex
 *   container. Mirrors CSS `flex: N`. On Compose Multiplatform this becomes
 *   `RowScope.weight` / `ColumnScope.weight` via captured parent scope; outside
 *   a flex container the property is a no-op (matching CSS).
 * @property opacity Opacity in `[0f, 1f]`.
 */
public data class Style(
    val padding: Padding? = null,
    val margin: Margin? = null,
    val backgroundColor: Color? = null,
    val backgroundGradient: LinearGradient? = null,
    val color: Color? = null,
    val fontSize: Sp? = null,
    val fontWeight: FontWeight? = null,
    val fontFamily: FontFamily? = null,
    val lineHeight: Sp? = null,
    val letterSpacing: Sp? = null,
    val textAlign: TextAlign? = null,
    val borderRadius: BorderRadius? = null,
    val border: Border? = null,
    val boxShadow: Shadow? = null,
    val flexDirection: FlexDirection? = null,
    val alignItems: Align? = null,
    val justifyContent: Justify? = null,
    val gap: Dp? = null,
    val width: Size? = null,
    val height: Size? = null,
    val flex: Float? = null,
    val opacity: Float? = null,
    // ── Token references ─────────────────────────────────────────────────────
    // A `*Ref` field is a CSS-variable name (e.g. "--uc-colors-accent") that
    // overrides the matching literal field at render time. Web emits
    // `var(--name)` directly. CMP looks up the value through the resolver
    // provided by the active theme (see unicompose-base UnicomposeTheme).
    //
    // The point of `*Ref` is to let top-level Style declarations reference
    // theme tokens without losing static-extractability — a String constant is
    // visible to the IR plugin in a way that `currentTokens().colors.accent`
    // (a runtime Composable read) is not.
    val colorRef: String? = null,
    val backgroundColorRef: String? = null,
    val gapRef: String? = null,
    val fontSizeRef: String? = null,
    /** Single CSS variable applied uniformly to all four padding sides. */
    val paddingAllRef: String? = null,
    /** Single CSS variable applied uniformly to all four corners. */
    val borderRadiusAllRef: String? = null,
    /**
     * Symmetric padding refs — vertical (top + bottom) and horizontal (left +
     * right). Lowers to the CSS `padding: var(--v) var(--h)` shorthand.
     * Useful when widget defaults use different token sizes per axis (e.g.
     * Button.Padding.symmetric(vertical = space.sm, horizontal = space.md)).
     */
    val paddingVerticalRef: String? = null,
    val paddingHorizontalRef: String? = null,
) {
    public companion object {
        /** A [Style] with no properties set. Equivalent to passing no style at all. */
        public val Empty: Style = Style()
    }

    /**
     * Merge two styles. For each property, a non-null value in [other] takes
     * precedence over the value in `this`. Useful for layering a base style with
     * call-site overrides.
     */
    public operator fun plus(other: Style): Style = Style(
        padding = other.padding ?: padding,
        margin = other.margin ?: margin,
        backgroundColor = other.backgroundColor ?: backgroundColor,
        backgroundGradient = other.backgroundGradient ?: backgroundGradient,
        color = other.color ?: color,
        fontSize = other.fontSize ?: fontSize,
        fontWeight = other.fontWeight ?: fontWeight,
        fontFamily = other.fontFamily ?: fontFamily,
        lineHeight = other.lineHeight ?: lineHeight,
        letterSpacing = other.letterSpacing ?: letterSpacing,
        textAlign = other.textAlign ?: textAlign,
        borderRadius = other.borderRadius ?: borderRadius,
        border = other.border ?: border,
        boxShadow = other.boxShadow ?: boxShadow,
        flexDirection = other.flexDirection ?: flexDirection,
        alignItems = other.alignItems ?: alignItems,
        justifyContent = other.justifyContent ?: justifyContent,
        gap = other.gap ?: gap,
        width = other.width ?: width,
        height = other.height ?: height,
        flex = other.flex ?: flex,
        opacity = other.opacity ?: opacity,
        colorRef = other.colorRef ?: colorRef,
        backgroundColorRef = other.backgroundColorRef ?: backgroundColorRef,
        gapRef = other.gapRef ?: gapRef,
        fontSizeRef = other.fontSizeRef ?: fontSizeRef,
        paddingAllRef = other.paddingAllRef ?: paddingAllRef,
        borderRadiusAllRef = other.borderRadiusAllRef ?: borderRadiusAllRef,
        paddingVerticalRef = other.paddingVerticalRef ?: paddingVerticalRef,
        paddingHorizontalRef = other.paddingHorizontalRef ?: paddingHorizontalRef,
    )
}

/**
 * Inside-edge spacing, in scale-independent pixels.
 *
 * Each side may differ. Use [all] for uniform padding or [symmetric] for
 * `vertical` / `horizontal` shorthand.
 */
public data class Padding(val top: Dp, val right: Dp, val bottom: Dp, val left: Dp) {
    public companion object {
        /** Padding with the same value on all four sides. */
        public fun all(value: Dp): Padding = Padding(value, value, value, value)

        /**
         * Padding with one value for top + bottom and another for left + right.
         * Defaults are zero.
         */
        public fun symmetric(vertical: Dp = 0.dp, horizontal: Dp = 0.dp): Padding =
            Padding(vertical, horizontal, vertical, horizontal)
    }
}

/**
 * Outside-edge spacing, in scale-independent pixels.
 *
 * On Compose Multiplatform this is implemented by wrapping the widget in an outer
 * `Box` with padding equal to the margin — there is no native child-level margin
 * modifier in Compose. CSS-style margin collapse between adjacent siblings is not
 * replicated.
 */
public data class Margin(val top: Dp, val right: Dp, val bottom: Dp, val left: Dp) {
    public companion object {
        /** Margin with the same value on all four sides. */
        public fun all(value: Dp): Margin = Margin(value, value, value, value)

        /**
         * Margin with one value for top + bottom and another for left + right.
         * Defaults are zero.
         */
        public fun symmetric(vertical: Dp = 0.dp, horizontal: Dp = 0.dp): Margin =
            Margin(vertical, horizontal, vertical, horizontal)
    }
}

/**
 * Per-corner radius for an element's rounded shape.
 *
 * Use [all] for uniform radius (the most common case). Pass per-corner values to
 * the constructor for asymmetric shapes — e.g. a top-rounded sheet:
 * `BorderRadius(topLeft = 16.dp, topRight = 16.dp, bottomRight = 0.dp, bottomLeft = 0.dp)`.
 */
public data class BorderRadius(
    val topLeft: Dp,
    val topRight: Dp,
    val bottomRight: Dp,
    val bottomLeft: Dp,
) {
    public companion object {
        /** Uniform corner radius on all four corners. */
        public fun all(value: Dp): BorderRadius = BorderRadius(value, value, value, value)

        /** Top corners get [top]; bottom corners get [bottom]. */
        public fun topBottom(top: Dp, bottom: Dp): BorderRadius =
            BorderRadius(top, top, bottom, bottom)
    }
}

/**
 * A linear color gradient — fill that smoothly transitions across the element
 * along [direction]. Mirrors CSS `linear-gradient`.
 *
 * On Compose Multiplatform this lowers to `Brush.linearGradient(colors, start, end)`
 * with start/end derived from [direction] and the element's bounds at draw time
 * — uses the built-in modifier surface, no custom drawing.
 *
 * On the web this emits CSS `background-image: linear-gradient(<direction>, …)`.
 *
 * Use as `Style(backgroundGradient = LinearGradient(...))`. When both
 * [Style.backgroundColor] and [Style.backgroundGradient] are set, the gradient
 * is rendered on top of the solid color (matching CSS behavior, where the
 * solid `background-color` paints first and `background-image` paints over it).
 *
 * @property direction Axis along which the gradient interpolates. See [GradientDirection].
 * @property colors Two or more colors to interpolate between. At least two required.
 * @property stops Optional fractional positions in `[0f, 1f]` for each color. When
 *   `null`, colors are spaced evenly. When provided, must have the same size as
 *   [colors].
 */
public data class LinearGradient(
    val direction: GradientDirection,
    val colors: List<Color>,
    val stops: List<Float>? = null,
) {
    init {
        require(colors.size >= 2) { "LinearGradient needs at least 2 colors, got ${colors.size}" }
        if (stops != null) {
            require(stops.size == colors.size) {
                "LinearGradient stops size (${stops.size}) must match colors size (${colors.size})"
            }
        }
    }
}

/**
 * Direction of a [LinearGradient] — which way the colors flow.
 *
 * Naming matches CSS `linear-gradient(to <direction>, …)`. `ToBottom` means
 * the first color is at the top and the last color is at the bottom.
 */
public enum class GradientDirection {
    ToTop,
    ToBottom,
    ToLeft,
    ToRight,
    ToTopLeft,
    ToTopRight,
    ToBottomLeft,
    ToBottomRight,
}

/**
 * A single edge of a [Border]. Use `null` for "no border on this side".
 *
 * @property width Edge thickness, drawn inward from the element's bounds.
 * @property color Stroke color.
 */
public data class BorderEdge(val width: Dp, val color: Color)

/**
 * Solid border around an element, with independent control per side.
 *
 * Use [Border.all] for the uniform case (the most common — same color and width
 * on all four sides), the named-argument constructor for asymmetric layouts:
 *
 * ```
 * Border.all(1.dp, Color.Black)
 * Border(bottom = BorderEdge(1.dp, dividerColor))
 * Border(top = BorderEdge(2.dp, accent), bottom = BorderEdge(1.dp, divider))
 * ```
 *
 * **Implementation note**: when all four edges are equal and non-null, the CMP
 * backend uses Compose's built-in `Modifier.border` (fast, follows the rounded
 * shape correctly). When edges differ — including any null edge — the backend
 * drops to `Modifier.drawBehind` and paints each edge as a rectangle. With
 * rounded corners and asymmetric edges, edges draw as straight lines clipped
 * by the corner radius; CSS-style miter joins between adjacent differently-
 * colored edges are not replicated (last-drawn-wins at the corner).
 *
 * @property top Edge along the top of the element, or null for no border.
 * @property right Edge along the right.
 * @property bottom Edge along the bottom.
 * @property left Edge along the left.
 */
public data class Border(
    val top: BorderEdge? = null,
    val right: BorderEdge? = null,
    val bottom: BorderEdge? = null,
    val left: BorderEdge? = null,
) {
    public companion object {
        /** Uniform border on all four sides. */
        public fun all(width: Dp, color: Color): Border {
            val edge = BorderEdge(width, color)
            return Border(top = edge, right = edge, bottom = edge, left = edge)
        }

        /** Same edge for top + bottom; same edge (possibly different) for left + right. */
        public fun symmetric(vertical: BorderEdge? = null, horizontal: BorderEdge? = null): Border =
            Border(top = vertical, right = horizontal, bottom = vertical, left = horizontal)
    }

    /** True when all four edges are present and equal — the fast path. */
    public val isUniform: Boolean
        get() = top != null && top == right && right == bottom && bottom == left
}

/**
 * Drop shadow behind an element. Mirrors CSS `box-shadow`'s parameter shape.
 *
 * **Two render paths on Compose Multiplatform**, picked by [blur]:
 *
 *  - **Hard shadow** ([blur] = 0): rendered via `Modifier.drawBehind` as a sharp
 *    offset rectangle behind the element. [offsetX] / [offsetY] / [spread] all
 *    work with full fidelity, matching CSS exactly.
 *  - **Blurred shadow** ([blur] > 0): rendered via Compose's `Modifier.shadow`
 *    with [blur] becoming the elevation. **In this path, [offsetX] / [offsetY] /
 *    [spread] are ignored on CMP.** The shadow is centered with elevation-style
 *    softness. This is the same approximation we shipped before; lifting it
 *    requires platform-specific Skia mask-filter access (see PLAN.md, post-v0.1).
 *
 * On the web, all four parameters (offset, blur, spread) lower to native CSS
 * `box-shadow` regardless of value.
 *
 * @property offsetX Horizontal shadow offset, positive = right.
 * @property offsetY Vertical shadow offset, positive = down.
 * @property blur Shadow blur radius. Set to `0.dp` for a sharp shadow; positive
 *   values soften and enlarge the shadow.
 * @property spread Shadow spread, positive grows the shadow outward, negative
 *   shrinks it. Honored only in the hard-shadow path on CMP; web honors it always.
 * @property color Shadow color, typically a low-alpha black or theme tint.
 */
public data class Shadow(
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val blur: Dp = 0.dp,
    val spread: Dp = 0.dp,
    val color: Color,
) {
    public companion object {
        /** Convenience for the centered elevation-style shadow shape (no offset, no spread). */
        public fun elevation(blur: Dp, color: Color): Shadow = Shadow(blur = blur, color = color)

        /** Convenience for a hard sharp drop shadow with no blur. */
        public fun drop(offsetX: Dp, offsetY: Dp, color: Color, spread: Dp = 0.dp): Shadow =
            Shadow(offsetX = offsetX, offsetY = offsetY, blur = 0.dp, spread = spread, color = color)
    }

    /** True when no blur is requested — eligible for the drawBehind fast path. */
    public val isHard: Boolean get() = blur.value == 0f
}

/**
 * Horizontal alignment of text within its line box.
 *
 * Maps to CSS `text-align` and Compose's `androidx.compose.ui.text.style.TextAlign`.
 */
public enum class TextAlign {
    /** Align to the start of the writing direction (left in LTR, right in RTL). */
    Start,

    /** Center on the line. */
    Center,

    /** Align to the end of the writing direction. */
    End,

    /** Stretch to both edges by adjusting word spacing (CSS `justify`). */
    Justify,
}

@JvmInline
public value class Dp(public val value: Float) {
    public companion object {
        /** Zero-length [Dp]. */
        public val Zero: Dp = Dp(0f)
    }
}

/** Construct a [Dp] from an integer literal — `16.dp`. */
public val Int.dp: Dp get() = Dp(this.toFloat())

/** Construct a [Dp] from a floating-point literal — `0.5f.dp`. */
public val Float.dp: Dp get() = Dp(this)

/**
 * Scale-independent pixel for typography.
 *
 * Differs from [Dp] in that it respects the user's system font-scale setting on
 * Android / iOS, and falls back to plain `px` on the web (no equivalent web concept).
 *
 * Construct via the [Int.sp] / [Float.sp] extensions: `14.sp`.
 */
@JvmInline
public value class Sp(public val value: Float)

/** Construct an [Sp] from an integer literal — `14.sp`. */
public val Int.sp: Sp get() = Sp(this.toFloat())

/** Construct an [Sp] from a floating-point literal — `13.5f.sp`. */
public val Float.sp: Sp get() = Sp(this)

/**
 * 32-bit ARGB color, packed as `0xAARRGGBB`.
 *
 * Construct with the [rgb] / [argb] factory functions, or use one of the named
 * constants on the companion object.
 *
 * @property argb Packed channel value. The high byte is alpha; remaining bytes are
 *   red, green, blue, in that order.
 */
@JvmInline
public value class Color(public val argb: Int) {
    /** Alpha channel, in `[0, 255]`. */
    public val alpha: Int get() = (argb ushr 24) and 0xFF

    /** Red channel, in `[0, 255]`. */
    public val red: Int get() = (argb ushr 16) and 0xFF

    /** Green channel, in `[0, 255]`. */
    public val green: Int get() = (argb ushr 8) and 0xFF

    /** Blue channel, in `[0, 255]`. */
    public val blue: Int get() = argb and 0xFF

    public companion object {
        /** Opaque black — `#000000`. */
        public val Black: Color = rgb(0, 0, 0)

        /** Opaque white — `#FFFFFF`. */
        public val White: Color = rgb(255, 255, 255)

        /** Fully transparent (zero in every channel). */
        public val Transparent: Color = Color(0)
    }
}

/**
 * Construct an opaque [Color] from individual `[0, 255]` channels.
 *
 * @param r Red channel.
 * @param g Green channel.
 * @param b Blue channel.
 */
public fun rgb(r: Int, g: Int, b: Int): Color =
    Color((0xFF shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF))

/**
 * Construct a [Color] with explicit alpha.
 *
 * @param a Alpha channel, in `[0, 255]` — 0 fully transparent, 255 opaque.
 * @param r Red channel.
 * @param g Green channel.
 * @param b Blue channel.
 */
public fun argb(a: Int, r: Int, g: Int, b: Int): Color =
    Color(((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF))

/**
 * Semantic font family.
 *
 * Each backend resolves the family to whatever the platform considers its
 * default of that kind:
 *  - Web (DOM): CSS generic family — `sans-serif`, `serif`, `monospace`.
 *    [Default] emits `system-ui` for the platform's UI font (San Francisco
 *    on macOS/iOS, Segoe UI on Windows, Roboto on Android, etc.).
 *  - Compose Multiplatform (Android, iOS, canvas web): Compose's
 *    `FontFamily.Default` / `SansSerif` / `Serif` / `Monospace`. On Android
 *    and iOS this picks up the system font; on canvas web it uses Skia's
 *    bundled font of that kind, which may not match the system font in the
 *    same browser.
 *
 * Exact font face therefore differs by backend; the family choice is
 * semantic. v0.1 doesn't support custom font resources — bundling a font
 * cross-platform is its own milestone.
 */
public enum class FontFamily {
    /** Platform UI font (browser `system-ui`; Compose `FontFamily.Default`). */
    Default,
    /** Generic sans-serif. */
    SansSerif,
    /** Generic serif. */
    Serif,
    /** Generic monospace. */
    Monospace,
}

/**
 * Type-weight for text rendering.
 *
 * Only the four most common weights are exposed. Each maps to its CSS numeric
 * equivalent on web and to `androidx.compose.ui.text.font.FontWeight` on Compose.
 *
 * @property value CSS numeric font-weight value.
 */
public enum class FontWeight(public val value: Int) {
    /** 400 — the standard non-bold weight. */
    Normal(400),

    /** 500. */
    Medium(500),

    /** 600. */
    SemiBold(600),

    /** 700. */
    Bold(700),
}

/**
 * Flex axis for a flex container — see [Style.flexDirection].
 *
 * Reverse modes (`row-reverse` / `column-reverse`) are intentionally omitted:
 * Compose `Row` / `Column` have no native reverse mode, and faking it via
 * `LayoutDirection.Rtl` inverts text direction inside, which is worse than not
 * supporting it. Reverse layouts are rare in app UI; users who genuinely need
 * them can `.reversed()` their children list in user code.
 */
public enum class FlexDirection {
    /** Children laid out horizontally, main axis is the X axis. */
    Row,

    /** Children laid out vertically, main axis is the Y axis. The unicompose default. */
    Column,
}

/**
 * Cross-axis alignment of children in a flex container — see [Style.alignItems].
 *
 * "Cross-axis" is the axis perpendicular to [Style.flexDirection]: vertical for a
 * Row, horizontal for a Column.
 */
public enum class Align {
    /** Align children to the start of the cross-axis. The default. */
    Start,

    /** Center children on the cross-axis. */
    Center,

    /** Align children to the end of the cross-axis. */
    End,

    /**
     * Stretch children to fill the cross-axis.
     *
     * On the web this is just CSS `align-items: stretch`. On Compose Multiplatform,
     * `Row` / `Column` have no native cross-axis stretch, so each child instead
     * applies `Modifier.fillMaxHeight()` / `Modifier.fillMaxWidth()` based on the
     * parent's direction (propagated via composition local).
     */
    Stretch,
}

/**
 * Main-axis arrangement of children in a flex container — see [Style.justifyContent].
 *
 * "Main-axis" is the axis defined by [Style.flexDirection]: horizontal for a Row,
 * vertical for a Column.
 */
public enum class Justify {
    /** Pack children at the start of the main axis. The default. */
    Start,

    /** Center children on the main axis. */
    Center,

    /** Pack children at the end of the main axis. */
    End,

    /** Equal space between adjacent children; no leading or trailing space. */
    SpaceBetween,

    /** Equal space around each child; outer halves leak to the container edges. */
    SpaceAround,

    /** Equal space between every child including container edges. */
    SpaceEvenly,
}

/**
 * Sizing mode for [Style.width] / [Style.height].
 *
 * The four modes mirror the equivalent web concepts — fixed pixels, full parent,
 * intrinsic content, and fractional — so that one [Style] value renders
 * equivalently on all backends.
 */
public sealed interface Size {
    /** A fixed [Dp] dimension. Equivalent to CSS `Npx`. */
    public data class Fixed(val dp: Dp) : Size

    /** Fill the available parent dimension. Equivalent to CSS `100%`. */
    public data object FillParent : Size

    /** Size to the intrinsic content. Equivalent to CSS `auto`. */
    public data object WrapContent : Size

    /**
     * Fractional fill of the parent dimension.
     *
     * @property value Fraction in `[0f, 1f]`. `0.5f` is half the parent.
     */
    public data class Fraction(val value: Float) : Size
}
