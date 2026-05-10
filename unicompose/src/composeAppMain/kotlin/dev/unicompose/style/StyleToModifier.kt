package dev.unicompose.style

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeUiSize
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp as composeDp

/**
 * Reduce a [Style] to a Compose `Modifier` chain.
 *
 * Order is fixed and intentional and follows CSS box-model semantics:
 *
 *   size → shadow → background → border → clip(borderRadius) → padding → opacity
 *
 * Why this order matters: in Compose, drawing modifiers (`background`, `border`,
 * `drawBehind`) paint at the *current* size in the chain, and `padding` shrinks
 * that size for everything after it. To match CSS — where background fills the
 * whole element and content is inset by padding — `background` and `border` must
 * appear *before* `padding`. Earlier we had padding first, which caused per-side
 * borders to draw inside the padded area (clipping content) and backgrounds to
 * paint only the inner box.
 *
 * Properties not handled here (margin, flex weight, alignment) are applied by
 * widget-specific code that knows the surrounding layout context.
 */
public fun Style.toModifier(): Modifier {
    var m: Modifier = Modifier

    when (val w = width) {
        is Size.Fixed -> m = m.width(w.dp.value.composeDp)
        Size.FillParent -> m = m.fillMaxWidth()
        Size.WrapContent -> m = m.wrapContentSize()
        is Size.Fraction -> m = m.fillMaxWidth(w.value)
        null -> {}
    }
    when (val h = height) {
        is Size.Fixed -> m = m.height(h.dp.value.composeDp)
        Size.FillParent -> m = m.fillMaxHeight()
        Size.WrapContent -> m = m.wrapContentSize()
        is Size.Fraction -> m = m.fillMaxHeight(h.value)
        null -> {}
    }

    val shape: Shape = borderRadius?.toShape() ?: RoundedCornerShape(0.composeDp)

    // Shadow first — paints behind everything else at the element's outer bounds.
    // Hard path: drawBehind with full offset+spread fidelity.
    // Blurred path: Modifier.shadow elevation approximation; offset/spread ignored.
    boxShadow?.let { s ->
        m = if (s.isHard) {
            val br = borderRadius
            m.drawBehind {
                val ox = s.offsetX.value * density
                val oy = s.offsetY.value * density
                val sp = s.spread.value * density
                val rect = Offset(ox - sp, oy - sp)
                val w = size.width + 2 * sp
                val h = size.height + 2 * sp
                if (br != null) {
                    drawRoundRect(
                        color = s.color.toComposeColor(),
                        topLeft = rect,
                        size = ComposeUiSize(w, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            (br.topLeft.value + sp).coerceAtLeast(0f) * density,
                        ),
                    )
                } else {
                    drawRect(color = s.color.toComposeColor(), topLeft = rect, size = ComposeUiSize(w, h))
                }
            }
        } else {
            m.shadow(
                elevation = s.blur.value.composeDp,
                shape = shape,
                ambientColor = s.color.toComposeColor(),
                spotColor = s.color.toComposeColor(),
            )
        }
    }

    // Background fills the element's outer bounds (CSS-like: backgrounds extend
    // under padding).
    backgroundColor?.let { c ->
        m = m.background(c.toComposeColor(), shape)
    }

    backgroundGradient?.let { g ->
        m = m.background(brush = g.toComposeBrush(), shape = shape)
    }

    // Border draws at the element's outer bounds, before any padding inset.
    border?.let { b ->
        m = if (b.isUniform) {
            // Fast path: built-in Modifier.border follows the rounded shape correctly.
            val edge = b.top!!
            m.border(edge.width.value.composeDp, edge.color.toComposeColor(), shape)
        } else {
            // Per-side borders: drop into custom drawing. Adjacent differently-colored
            // edges meet at corners with last-drawn-wins overlap (no CSS-style miter).
            m.drawBehind {
                fun drawEdgeRect(topLeft: Offset, w: Float, h: Float, color: Color) {
                    drawRect(
                        color = color.toComposeColor(),
                        topLeft = topLeft,
                        size = ComposeUiSize(w, h),
                    )
                }
                b.top?.let { e ->
                    val w = e.width.value * density
                    drawEdgeRect(Offset.Zero, size.width, w, e.color)
                }
                b.right?.let { e ->
                    val w = e.width.value * density
                    drawEdgeRect(Offset(size.width - w, 0f), w, size.height, e.color)
                }
                b.bottom?.let { e ->
                    val w = e.width.value * density
                    drawEdgeRect(Offset(0f, size.height - w), size.width, w, e.color)
                }
                b.left?.let { e ->
                    val w = e.width.value * density
                    drawEdgeRect(Offset.Zero, w, size.height, e.color)
                }
            }
        }
    }

    // Clip with the rounded shape after background+border so content respects
    // the corner radius even when it wouldn't otherwise overflow.
    if (borderRadius != null) {
        m = m.clip(shape)
    }

    // Padding goes last (apart from opacity) — only the content inside is inset.
    padding?.let { p ->
        m = m.padding(
            start = p.left.value.composeDp,
            top = p.top.value.composeDp,
            end = p.right.value.composeDp,
            bottom = p.bottom.value.composeDp,
        )
    }

    opacity?.let { o ->
        m = m.alpha(o)
    }

    return m
}

/** Translate a [BorderRadius] to a Compose `RoundedCornerShape`. */
internal fun BorderRadius.toShape(): RoundedCornerShape = RoundedCornerShape(
    topStart = topLeft.value.composeDp,
    topEnd = topRight.value.composeDp,
    bottomEnd = bottomRight.value.composeDp,
    bottomStart = bottomLeft.value.composeDp,
)

/**
 * Translate a [LinearGradient] to a Compose `Brush.linearGradient`.
 *
 * Compose uses pixel-space `start` / `end` offsets; we use `Float.POSITIVE_INFINITY`
 * which Compose interprets as "the end of the painted area" along that axis.
 * The combination produces the requested direction for any element size.
 */
internal fun LinearGradient.toComposeBrush(): androidx.compose.ui.graphics.Brush {
    val (start, end) = direction.toComposeOffsets()
    val composeColors = colors.map { it.toComposeColor() }
    val localStops = stops
    return if (localStops != null) {
        val pairs = localStops.zip(composeColors).map { (s, c) -> s to c }.toTypedArray()
        androidx.compose.ui.graphics.Brush.linearGradient(*pairs, start = start, end = end)
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(composeColors, start = start, end = end)
    }
}

private fun GradientDirection.toComposeOffsets(): Pair<Offset, Offset> {
    val inf = Float.POSITIVE_INFINITY
    return when (this) {
        GradientDirection.ToTop -> Offset(0f, inf) to Offset.Zero
        GradientDirection.ToBottom -> Offset.Zero to Offset(0f, inf)
        GradientDirection.ToLeft -> Offset(inf, 0f) to Offset.Zero
        GradientDirection.ToRight -> Offset.Zero to Offset(inf, 0f)
        GradientDirection.ToTopLeft -> Offset(inf, inf) to Offset.Zero
        GradientDirection.ToTopRight -> Offset(0f, inf) to Offset(inf, 0f)
        GradientDirection.ToBottomLeft -> Offset(inf, 0f) to Offset(0f, inf)
        GradientDirection.ToBottomRight -> Offset.Zero to Offset(inf, inf)
    }
}

/**
 * Lowers our typed [Color] to Compose's. [Color.Ref] returns
 * `ComposeColor.Unspecified` because CMP has no `var()` mechanism — refs must
 * be resolved against the active theme upstream (see unicompose-base
 * Style.resolveRefs / Color.resolved). If a Ref reaches this function it
 * means resolution didn't happen, which is a programming error in the call
 * chain rather than something to silently default.
 */
internal fun Color.toComposeColor(): ComposeColor = when (this) {
    is Color.Literal -> ComposeColor(argb)
    is Color.Ref -> ComposeColor.Unspecified
}
