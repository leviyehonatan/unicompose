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
 * Order is fixed and intentional — see plan, "How identity is enforced":
 * size → shadow → padding → background → border → clip(borderRadius) → opacity.
 * The fixed order neutralizes Compose `Modifier`'s order sensitivity so that two
 * equal [Style] values always produce visually equal output.
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

    // Shadow before padding so it draws around the element's bounds, not inside.
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

    padding?.let { p ->
        m = m.padding(
            start = p.left.value.composeDp,
            top = p.top.value.composeDp,
            end = p.right.value.composeDp,
            bottom = p.bottom.value.composeDp,
        )
    }

    backgroundColor?.let { c ->
        m = m.background(c.toComposeColor(), shape)
    }

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

    if (borderRadius != null) {
        m = m.clip(shape)
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

internal fun Color.toComposeColor(): ComposeColor = ComposeColor(argb)
