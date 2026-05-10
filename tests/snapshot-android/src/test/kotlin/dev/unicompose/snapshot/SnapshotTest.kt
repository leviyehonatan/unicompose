package dev.unicompose.snapshot

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import dev.unicompose.UiBox
import dev.unicompose.UiCheckbox
import dev.unicompose.UiColumn
import dev.unicompose.UiRow
import dev.unicompose.UiText
import dev.unicompose.UiTextField
import dev.unicompose.base.Badge
import dev.unicompose.base.Button
import dev.unicompose.base.ButtonVariant
import dev.unicompose.base.Card
import dev.unicompose.base.DarkTokens
import dev.unicompose.base.H1
import dev.unicompose.base.H2
import dev.unicompose.base.LightTokens
import dev.unicompose.base.UnicomposeTheme
import dev.unicompose.base.currentTokens
import dev.unicompose.style.Align
import dev.unicompose.style.Justify
import dev.unicompose.style.Padding
import dev.unicompose.style.Size
import dev.unicompose.style.Style
import org.junit.Rule
import org.junit.Test

/**
 * Paparazzi snapshot tests for the Android render path. Goldens live under
 * src/test/snapshots/images/ — re-record with `recordPaparazziDebug`.
 *
 * Two scenes (kitchen-sink-style widget gallery + todo-list shape), each in
 * light + dark, gives four goldens. Catches regressions in widget rendering,
 * theme application, and the Modifier reducer that the web Playwright suite
 * doesn't see (different render backend — CMP/Skia on Android vs DOM on web).
 */
class SnapshotTest {

    @get:Rule
    val paparazzi: Paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test fun widgetsLight() = paparazzi.snapshot {
        UnicomposeTheme(tokens = LightTokens) { WidgetGallery() }
    }

    @Test fun widgetsDark() = paparazzi.snapshot {
        UnicomposeTheme(tokens = DarkTokens) { WidgetGallery() }
    }

    @Test fun todoListLight() = paparazzi.snapshot {
        UnicomposeTheme(tokens = LightTokens) { TodoListScene() }
    }

    @Test fun todoListDark() = paparazzi.snapshot {
        UnicomposeTheme(tokens = DarkTokens) { TodoListScene() }
    }
}

/** Compact tour of every base widget. */
@Composable
private fun WidgetGallery() {
    val t = currentTokens()
    UiColumn(
        style = Style(
            backgroundColor = t.colors.bgPage,
            padding = Padding.all(t.space.lg),
            gap = t.space.md,
            width = Size.FillParent,
            height = Size.FillParent,
        ),
    ) {
        H1("unicompose")
        H2("widgets")
        Card {
            UiColumn(style = Style(gap = t.space.sm, width = Size.FillParent)) {
                UiRow(style = Style(gap = t.space.sm, alignItems = Align.Center)) {
                    Button(onClick = {}) { UiText("Primary") }
                    Button(onClick = {}, variant = ButtonVariant.Secondary) { UiText("Secondary") }
                    Button(onClick = {}, variant = ButtonVariant.Ghost) { UiText("Ghost") }
                }
                UiRow(style = Style(gap = t.space.sm, alignItems = Align.Center)) {
                    Badge { UiText("new") }
                    Badge { UiText("12") }
                    Badge { UiText("beta") }
                }
                UiRow(style = Style(gap = t.space.sm, alignItems = Align.Center)) {
                    UiCheckbox(checked = true, onCheckedChange = {})
                    UiText("checkbox on")
                    UiCheckbox(checked = false, onCheckedChange = {})
                    UiText("checkbox off")
                }
                UiTextField(value = "hello", onValueChange = {}, placeholder = "type…")
            }
        }
    }
}

/** Snapshot of the todo-app shape, with a small, deterministic data set. */
@Composable
private fun TodoListScene() {
    val t = currentTokens()
    val items = listOf(
        "Try the unicompose dark-mode toggle" to false,
        "Tap a todo's checkbox" to true,
        "Add a new item below" to false,
    )
    UiColumn(
        style = Style(
            backgroundColor = t.colors.bgPage,
            padding = Padding.all(t.space.lg),
            gap = t.space.lg,
            width = Size.FillParent,
            height = Size.FillParent,
        ),
    ) {
        UiRow(
            style = Style(
                justifyContent = Justify.SpaceBetween,
                alignItems = Align.Center,
                width = Size.FillParent,
                gap = t.space.sm,
            ),
        ) {
            UiColumn(style = Style(gap = t.space.xs)) {
                H1("todos")
                UiText(
                    "a unicompose v0.1 demo",
                    style = Style(fontSize = t.type.sm, color = t.colors.textSecondary),
                )
            }
            Button(onClick = {}, variant = ButtonVariant.Secondary) { UiText("Dark") }
        }
        Card {
            UiRow(style = Style(gap = t.space.sm, width = Size.FillParent, alignItems = Align.Center)) {
                UiBox(style = Style(flex = 1f)) {
                    UiTextField(value = "", onValueChange = {}, placeholder = "What needs doing?")
                }
                Button(onClick = {}) { UiText("Add") }
            }
        }
        Card {
            UiColumn(style = Style(width = Size.FillParent, gap = t.space.sm)) {
                items.forEach { (text, done) ->
                    UiRow(style = Style(gap = t.space.sm, alignItems = Align.Center, width = Size.FillParent)) {
                        UiCheckbox(checked = done, onCheckedChange = {})
                        UiBox(style = Style(flex = 1f)) {
                            UiText(
                                text,
                                style = Style(color = if (done) t.colors.textSecondary else t.colors.textPrimary),
                            )
                        }
                    }
                }
            }
        }
    }
}
