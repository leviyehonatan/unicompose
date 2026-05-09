package dev.unicompose.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.unicompose.UiButton
import dev.unicompose.UiColumn
import dev.unicompose.UiRow
import dev.unicompose.UiText
import dev.unicompose.base.Badge
import dev.unicompose.base.Card
import dev.unicompose.base.DarkTokens
import dev.unicompose.base.LightTokens
import dev.unicompose.base.UnicomposeTheme
import dev.unicompose.base.currentTokens
import dev.unicompose.style.Border
import dev.unicompose.style.BorderEdge
import dev.unicompose.style.BorderRadius
import dev.unicompose.style.FontWeight
import dev.unicompose.style.GradientDirection
import dev.unicompose.style.Justify
import dev.unicompose.style.LinearGradient
import dev.unicompose.style.Padding
import dev.unicompose.style.Shadow
import dev.unicompose.style.Size
import dev.unicompose.style.Style
import dev.unicompose.style.argb
import dev.unicompose.style.dp
import dev.unicompose.style.rgb

/**
 * Sample app — wraps everything in `UnicomposeTheme` from `unicompose-base`.
 * The `unicompose` library itself is just primitives; the theme + Card + Badge
 * come from the design system on top.
 */
@Composable
public fun App() {
    var dark by remember { mutableStateOf(false) }
    UnicomposeTheme(tokens = if (dark) DarkTokens else LightTokens) {
        Page(toggleDark = { dark = !dark }, isDark = dark)
    }
}

@Composable
private fun Page(toggleDark: () -> Unit, isDark: Boolean) {
    val t = currentTokens()
    UiColumn(
        style = Style(
            backgroundColor = t.colors.bgPage,
            padding = Padding.all(t.space.lg),
            gap = t.space.md,
            width = Size.FillParent,
        ),
    ) {
        Header(toggleDark, isDark)
        StatRow()
        Notice()
        Card {
            UiText(
                "Layout works the same on three platforms.",
                style = Style(fontSize = t.type.md, fontWeight = FontWeight.SemiBold),
            )
            UiText(
                "This screen is one Composable in commonMain. " +
                    "Android and iOS render via Compose Multiplatform (Skia). " +
                    "The web renders as real DOM with hashed atomic CSS classes.",
                style = Style(fontSize = t.type.sm, color = t.colors.textSecondary),
            )
        }
        TagRow()
    }
}

@Composable
private fun Header(toggleDark: () -> Unit, isDark: Boolean) {
    val t = currentTokens()
    UiRow(
        style = Style(
            justifyContent = Justify.SpaceBetween,
            width = Size.FillParent,
            gap = t.space.sm,
        ),
    ) {
        UiColumn(style = Style(gap = t.space.xs)) {
            UiText("unicompose", style = Style(fontSize = t.type.xl, fontWeight = FontWeight.Bold))
            UiText(
                "kitchen-sink — design system demo",
                style = Style(fontSize = t.type.sm, color = t.colors.textSecondary),
            )
        }
        UiButton(
            onClick = toggleDark,
            // Gradient background — exercises the new linear-gradient path on
            // all three platforms. CMP uses Brush.linearGradient through
            // Modifier.background; web emits a CSS linear-gradient.
            style = Style(
                backgroundGradient = LinearGradient(
                    direction = GradientDirection.ToBottomRight,
                    colors = listOf(
                        t.colors.accent,
                        rgb(t.colors.accent.red, t.colors.accent.green, (t.colors.accent.blue * 0.7).toInt()),
                    ),
                ),
                padding = Padding.symmetric(vertical = t.space.sm, horizontal = t.space.md),
                borderRadius = BorderRadius.all(t.radii.md),
            ),
        ) {
            UiText(
                if (isDark) "Light" else "Dark",
                style = Style(color = t.colors.onAccent, fontSize = t.type.sm, fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Composable
private fun Notice() {
    val t = currentTokens()
    // Demonstrates per-side border: a left accent bar plus a subtle bottom hairline.
    // CMP draws via Modifier.drawBehind; web emits border-left + border-bottom shorthands.
    UiRow(
        style = Style(
            backgroundColor = t.colors.bgSubtle,
            padding = Padding.symmetric(vertical = t.space.sm, horizontal = t.space.md),
            border = Border(
                left = BorderEdge(width = 3.dp, color = t.colors.accent),
                bottom = BorderEdge(width = 1.dp, color = t.colors.borderSubtle),
            ),
            // Hard offset shadow — exercises the drawBehind path with full
            // offset+spread fidelity on CMP. blur = 0, so we skip Modifier.shadow.
            boxShadow = Shadow.drop(offsetX = 2.dp, offsetY = 2.dp, color = argb(20, 0, 0, 0)),
            width = Size.FillParent,
        ),
    ) {
        UiText(
            "Per-side borders + hard drop shadows work on three platforms via custom drawing.",
            style = Style(fontSize = t.type.sm, color = t.colors.textSecondary),
        )
    }
}

@Composable
private fun StatRow() {
    val t = currentTokens()
    UiRow(
        style = Style(
            gap = t.space.sm,
            justifyContent = Justify.SpaceBetween,
            width = Size.FillParent,
        ),
    ) {
        Stat(label = "widgets", value = "13")
        Stat(label = "platforms", value = "3")
        Stat(label = "shared", value = "100%")
    }
}

@Composable
private fun Stat(label: String, value: String) {
    val t = currentTokens()
    Card(style = Style(padding = Padding.symmetric(vertical = t.space.sm, horizontal = t.space.md), gap = t.space.xs)) {
        UiText(value, style = Style(fontSize = t.type.lg, fontWeight = FontWeight.SemiBold))
        UiText(label, style = Style(fontSize = t.type.xs, color = t.colors.textSecondary))
    }
}

@Composable
private fun TagRow() {
    val t = currentTokens()
    UiRow(style = Style(gap = t.space.sm)) {
        Badge("Android")
        Badge("iOS")
        Badge("Web (DOM)")
    }
}
