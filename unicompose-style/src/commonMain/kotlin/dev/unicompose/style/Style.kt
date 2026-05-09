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
 * @property backgroundColor Solid fill behind the element's content.
 * @property color Foreground color used for text by default.
 * @property fontSize Text size in scale-independent pixels.
 * @property fontWeight Text weight — see [FontWeight] for supported values.
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
    val color: Color? = null,
    val fontSize: Sp? = null,
    val fontWeight: FontWeight? = null,
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
        color = other.color ?: color,
        fontSize = other.fontSize ?: fontSize,
        fontWeight = other.fontWeight ?: fontWeight,
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
 * Solid border around an element.
 *
 * v0.1 supports uniform borders only — same color and width on all four sides.
 * The border respects [Style.borderRadius] when both are set: the border draws
 * along the rounded shape.
 *
 * Per-side borders (different widths/colors per edge) are deferred to a later
 * milestone since Compose's `Modifier.border` is uniform; per-side requires
 * custom drawing on the CMP backend.
 */
public data class Border(val width: Dp, val color: Color)

/**
 * Drop shadow behind an element.
 *
 * **Cross-platform caveat**: CSS `box-shadow` supports per-axis offset
 * (`offsetX`, `offsetY`); Compose `Modifier.shadow` is elevation-based and
 * ignores horizontal/vertical offsets. The two backends produce *visually
 * equivalent* shadows (similar size and softness for the same [blur] value)
 * but not pixel-equivalent. If precise drop-shadow positioning matters,
 * drop down to platform-specific styling.
 *
 * Spread radius (CSS `box-shadow` 4th arg) is omitted — no Compose equivalent.
 *
 * @property blur Shadow blur radius / Compose elevation. Higher = softer + larger.
 * @property color Shadow color, typically a low-alpha black or theme tint.
 */
public data class Shadow(val blur: Dp, val color: Color)

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
