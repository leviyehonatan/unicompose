package dev.unicompose.sample

import androidx.compose.runtime.Composable
import dev.unicompose.UiBox
import dev.unicompose.UiColumn
import dev.unicompose.UiRow
import dev.unicompose.UiText
import dev.unicompose.style.Align
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Justify
import dev.unicompose.style.Padding
import dev.unicompose.style.Size
import dev.unicompose.style.Style
import dev.unicompose.style.dp
import dev.unicompose.style.rgb
import dev.unicompose.style.sp

private val Bg = rgb(0xF7, 0xF7, 0xF8)
private val CardBg = rgb(0xFF, 0xFF, 0xFF)
private val Ink = rgb(0x14, 0x14, 0x14)
private val Subtle = rgb(0x6B, 0x6F, 0x76)
private val Accent = rgb(0x35, 0x6D, 0xF5)
private val PillBg = rgb(0xEE, 0xF1, 0xFA)

@Composable
public fun App() {
    UiColumn(
        style = Style(
            backgroundColor = Bg,
            padding = Padding.all(20.dp),
            gap = 16.dp,
            width = Size.FillParent,
        ),
    ) {
        Header()
        StatRow()
        Card(
            title = "Layout works the same on three platforms.",
            body = "This screen is one Composable in commonMain. Android and iOS render via Compose Multiplatform (Skia). The web renders as real DOM with hashed atomic CSS classes — view source to verify.",
        )
        TagRow()
    }
}

@Composable
private fun Header() {
    UiColumn(style = Style(gap = 4.dp)) {
        UiText(
            "unicompose",
            style = Style(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink),
        )
        UiText(
            "kitchen-sink — M1 layout demo",
            style = Style(fontSize = 13.sp, color = Subtle),
        )
    }
}

@Composable
private fun StatRow() {
    UiRow(
        style = Style(
            gap = 12.dp,
            justifyContent = Justify.SpaceBetween,
            width = Size.FillParent,
        ),
    ) {
        Stat(label = "widgets", value = "4")
        Stat(label = "platforms", value = "3")
        Stat(label = "shared", value = "100%")
    }
}

@Composable
private fun Stat(label: String, value: String) {
    UiColumn(
        style = Style(
            backgroundColor = CardBg,
            padding = Padding.symmetric(vertical = 12.dp, horizontal = 14.dp),
            borderRadius = 10.dp,
            gap = 2.dp,
            alignItems = Align.Start,
        ),
    ) {
        UiText(value, style = Style(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink))
        UiText(label, style = Style(fontSize = 11.sp, color = Subtle))
    }
}

@Composable
private fun Card(title: String, body: String) {
    UiColumn(
        style = Style(
            backgroundColor = CardBg,
            padding = Padding.all(16.dp),
            borderRadius = 12.dp,
            gap = 8.dp,
            width = Size.FillParent,
        ),
    ) {
        UiText(title, style = Style(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink))
        UiText(body, style = Style(fontSize = 13.sp, color = Subtle))
    }
}

@Composable
private fun TagRow() {
    UiRow(style = Style(gap = 8.dp, alignItems = Align.Center)) {
        Tag("Android")
        Tag("iOS")
        Tag("Web (DOM)")
    }
}

@Composable
private fun Tag(text: String) {
    UiBox(
        style = Style(
            backgroundColor = PillBg,
            padding = Padding.symmetric(vertical = 4.dp, horizontal = 10.dp),
            borderRadius = 999.dp,
        ),
    ) {
        UiText(text, style = Style(fontSize = 12.sp, color = Accent, fontWeight = FontWeight.Medium))
    }
}
