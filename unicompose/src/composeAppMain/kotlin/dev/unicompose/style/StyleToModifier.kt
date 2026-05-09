package dev.unicompose.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp as composeDp

/**
 * Reduce a [Style] to a Compose `Modifier` chain.
 *
 * Order is fixed and intentional — see plan, "How identity is enforced":
 * size → padding → background → clip(borderRadius) → opacity. The fixed order
 * neutralizes Compose `Modifier`'s order sensitivity so that two equal [Style]
 * values always produce visually equal output.
 *
 * Properties not yet implemented (margin, fontSize on text-level, flex layout) are
 * applied by widget-specific code that knows the surrounding layout context.
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

    padding?.let { p ->
        m = m.padding(
            start = p.left.value.composeDp,
            top = p.top.value.composeDp,
            end = p.right.value.composeDp,
            bottom = p.bottom.value.composeDp,
        )
    }

    backgroundColor?.let { c ->
        m = m.background(c.toComposeColor())
    }

    borderRadius?.let { r ->
        m = m.clip(RoundedCornerShape(r.value.composeDp))
    }

    opacity?.let { o ->
        m = m.alpha(o)
    }

    return m
}

internal fun Color.toComposeColor(): ComposeColor = ComposeColor(argb)
