package dev.unicompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp as composeDp
import dev.unicompose.internal.FlexParent
import dev.unicompose.internal.LocalColumnScope
import dev.unicompose.internal.LocalFlexParent
import dev.unicompose.internal.LocalRowScope
import dev.unicompose.style.Align
import dev.unicompose.style.FlexDirection
import dev.unicompose.style.Justify
import dev.unicompose.style.Style
import dev.unicompose.style.toModifier

@Composable
public actual fun UiBox(style: Style, content: @Composable () -> Unit) {
    val parentRow = LocalRowScope.current
    val parentColumn = LocalColumnScope.current
    val parentInfo = LocalFlexParent.current

    val direction = style.flexDirection ?: FlexDirection.Column
    val stretchChildren = style.alignItems == Align.Stretch
    val newScope = FlexParent(direction = direction, stretchChildren = stretchChildren)

    // Outer modifier carries layout-with-respect-to-parent concerns: margin,
    // weight (style.flex), cross-axis stretch from parent's alignItems = Stretch.
    var outer: Modifier = Modifier

    style.flex?.let { f ->
        outer = when {
            parentRow != null -> with(parentRow) { outer.weight(f) }
            parentColumn != null -> with(parentColumn) { outer.weight(f) }
            else -> outer // outside a flex container; matches CSS where flex on a
                          // non-flex-child is also a no-op.
        }
    }

    val parentWantsStretch = parentInfo?.stretchChildren == true && style.alignItems == null
    if (parentWantsStretch) {
        outer = when (parentInfo!!.direction) {
            FlexDirection.Row -> outer.fillMaxHeight()
            FlexDirection.Column -> outer.fillMaxWidth()
        }
    }

    style.margin?.let { m ->
        outer = outer.padding(
            start = m.left.value.composeDp,
            top = m.top.value.composeDp,
            end = m.right.value.composeDp,
            bottom = m.bottom.value.composeDp,
        )
    }

    // Inner modifier is the visual properties: size, padding, background, borderRadius, opacity.
    val inner = style.toModifier()

    val needsOuterWrapper = style.flex != null || style.margin != null || parentWantsStretch

    val renderInner: @Composable () -> Unit = {
        when (direction) {
            FlexDirection.Row -> Row(
                modifier = inner,
                horizontalArrangement = mainAxisHorizontal(style.justifyContent, style.gap?.value),
                verticalAlignment = crossAxisVertical(style.alignItems),
            ) {
                ProvideScopes(rowScope = this, columnScope = null, parent = newScope) {
                    content()
                }
            }
            FlexDirection.Column -> Column(
                modifier = inner,
                verticalArrangement = mainAxisVertical(style.justifyContent, style.gap?.value),
                horizontalAlignment = crossAxisHorizontal(style.alignItems),
            ) {
                ProvideScopes(rowScope = null, columnScope = this, parent = newScope) {
                    content()
                }
            }
        }
    }

    if (needsOuterWrapper) {
        Box(modifier = outer) { renderInner() }
    } else {
        renderInner()
    }
}

@Composable
private inline fun ProvideScopes(
    rowScope: RowScope?,
    columnScope: ColumnScope?,
    parent: FlexParent,
    crossinline content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalRowScope provides rowScope,
        LocalColumnScope provides columnScope,
        LocalFlexParent provides parent,
    ) { content() }
}

private fun mainAxisHorizontal(justify: Justify?, gapPx: Float?): Arrangement.Horizontal {
    val gap = gapPx?.let { Arrangement.spacedBy(it.composeDp) }
    return when (justify) {
        null -> gap ?: Arrangement.Start
        Justify.Start -> gap ?: Arrangement.Start
        Justify.Center -> Arrangement.Center
        Justify.End -> Arrangement.End
        Justify.SpaceBetween -> Arrangement.SpaceBetween
        Justify.SpaceAround -> Arrangement.SpaceAround
        Justify.SpaceEvenly -> Arrangement.SpaceEvenly
    }
}

private fun mainAxisVertical(justify: Justify?, gapPx: Float?): Arrangement.Vertical {
    val gap = gapPx?.let { Arrangement.spacedBy(it.composeDp) }
    return when (justify) {
        null -> gap ?: Arrangement.Top
        Justify.Start -> gap ?: Arrangement.Top
        Justify.Center -> Arrangement.Center
        Justify.End -> Arrangement.Bottom
        Justify.SpaceBetween -> Arrangement.SpaceBetween
        Justify.SpaceAround -> Arrangement.SpaceAround
        Justify.SpaceEvenly -> Arrangement.SpaceEvenly
    }
}

private fun crossAxisVertical(align: Align?): Alignment.Vertical = when (align) {
    null, Align.Start -> Alignment.Top
    Align.Center -> Alignment.CenterVertically
    Align.End -> Alignment.Bottom
    // Stretch: handled per-child via LocalFlexParent + fillMaxHeight on each child.
    // Compose's Row has no native cross-axis stretch; we fall back to Top alignment
    // here and let the child's own outer modifier expand it.
    Align.Stretch -> Alignment.Top
}

private fun crossAxisHorizontal(align: Align?): Alignment.Horizontal = when (align) {
    null, Align.Start -> Alignment.Start
    Align.Center -> Alignment.CenterHorizontally
    Align.End -> Alignment.End
    Align.Stretch -> Alignment.Start
}
