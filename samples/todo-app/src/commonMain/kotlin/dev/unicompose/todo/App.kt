package dev.unicompose.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.unicompose.base.LightTokens
import dev.unicompose.base.TokenRefs
import dev.unicompose.base.UnicomposeTheme
import dev.unicompose.style.Align
import dev.unicompose.style.Color
import dev.unicompose.style.Dp
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Justify
import dev.unicompose.style.Padding
import dev.unicompose.style.Size
import dev.unicompose.style.Sp
import dev.unicompose.style.Style

/**
 * Single-screen todo app. Exercises:
 *  - UnicomposeTheme + dark-mode toggle (LightTokens / DarkTokens swap)
 *  - H1 from unicompose-base
 *  - Card and Badge themed widgets
 *  - Button (Primary / Ghost variants)
 *  - UiCheckbox, UiTextField (stateful input primitives)
 *  - Layout primitives (UiBox, UiRow, UiColumn) with gap, justify, align
 *  - **TokenRefs everywhere** so the unicompose-css-extractor IR plugin
 *    extracts every Style call to the static `unicompose-generated.css`
 *    instead of routing through the runtime AtomicCss path. Theme switches
 *    just rewrite CSS variables on <html>; no per-Style recomposition.
 *
 * Multi-screen / persistence / virtualized list defer to post-v0.1 — they need
 * UiNavHost, Multiplatform-Settings, and UiLazyColumn respectively.
 */
@Composable
public fun App() {
    var dark by remember { mutableStateOf(false) }
    val state = remember { TodoState() }

    UnicomposeTheme(tokens = if (dark) DarkTokens else LightTokens) {
        Page(
            state = state,
            isDark = dark,
            toggleDark = { dark = !dark },
        )
    }
}

// ── Top-level Style declarations (statically extractable) ────────────────

private val PageStyle: Style = Style(
    backgroundColor = Color.token(TokenRefs.colors.bgPage),
    padding = Padding.all(Dp.token(TokenRefs.space.lg)),
    gap = Dp.token(TokenRefs.space.lg),
    width = Size.FillParent,
    height = Size.FillParent,
    alignItems = Align.Stretch,
)

private val HeaderRowStyle: Style = Style(
    justifyContent = Justify.SpaceBetween,
    alignItems = Align.Center,
    width = Size.FillParent,
    gap = Dp.token(TokenRefs.space.sm),
)

private val HeaderTitleColumnStyle: Style = Style(
    gap = Dp.token(TokenRefs.space.xs),
)

private val HeaderSubtitleStyle: Style = Style(
    fontSize = Sp.token(TokenRefs.type.sm),
    color = Color.token(TokenRefs.colors.textSecondary),
)

private val AddTodoFormRowStyle: Style = Style(
    gap = Dp.token(TokenRefs.space.sm),
    width = Size.FillParent,
    alignItems = Align.Center,
)

private val FlexFillStyle: Style = Style(flex = 1f)

private val EmptyTodoTextStyle: Style = Style(
    color = Color.token(TokenRefs.colors.textSecondary),
)

private val TodoListColumnStyle: Style = Style(
    width = Size.FillParent,
    gap = Dp.token(TokenRefs.space.sm),
)

private val TodoRowStyle: Style = Style(
    gap = Dp.token(TokenRefs.space.sm),
    alignItems = Align.Center,
    width = Size.FillParent,
)

private val TodoTextDoneStyle: Style = Style(
    color = Color.token(TokenRefs.colors.textSecondary),
)

private val TodoTextActiveStyle: Style = Style(
    color = Color.token(TokenRefs.colors.textPrimary),
)

private val DeleteIconStyle: Style = Style(
    fontSize = Sp.token(TokenRefs.type.lg),
    fontWeight = FontWeight.Bold,
)

private val FooterRowStyle: Style = Style(
    justifyContent = Justify.SpaceBetween,
    alignItems = Align.Center,
    width = Size.FillParent,
    gap = Dp.token(TokenRefs.space.sm),
)

private val FooterBadgeRowStyle: Style = Style(
    gap = Dp.token(TokenRefs.space.sm),
    alignItems = Align.Center,
)

// ── Composable wrappers ──────────────────────────────────────────────────

@Composable
private fun Page(state: TodoState, isDark: Boolean, toggleDark: () -> Unit) {
    UiColumn(style = PageStyle) {
        Header(isDark = isDark, toggleDark = toggleDark)
        AddTodoForm(state)
        TodoList(state)
        if (state.items.isNotEmpty()) {
            Footer(state)
        }
    }
}

@Composable
private fun Header(isDark: Boolean, toggleDark: () -> Unit) {
    UiRow(style = HeaderRowStyle) {
        UiColumn(style = HeaderTitleColumnStyle) {
            H1("todos")
            UiText("a unicompose v0.1 demo", style = HeaderSubtitleStyle)
        }
        Button(onClick = toggleDark, variant = ButtonVariant.Secondary) {
            UiText(if (isDark) "Light" else "Dark")
        }
    }
}

@Composable
private fun AddTodoForm(state: TodoState) {
    Card {
        UiRow(style = AddTodoFormRowStyle) {
            UiBox(style = FlexFillStyle) {
                UiTextField(
                    value = state.draft,
                    onValueChange = { state.draft = it },
                    placeholder = "What needs doing?",
                )
            }
            Button(onClick = { state.addDraft() }) { UiText("Add") }
        }
    }
}

@Composable
private fun TodoList(state: TodoState) {
    if (state.items.isEmpty()) {
        Card {
            UiText("Your list is empty — add something above.", style = EmptyTodoTextStyle)
        }
        return
    }
    Card {
        UiColumn(style = TodoListColumnStyle) {
            state.items.forEach { todo ->
                TodoRow(
                    todo = todo,
                    onToggle = { state.toggle(todo.id) },
                    onDelete = { state.delete(todo.id) },
                )
            }
        }
    }
}

@Composable
private fun TodoRow(todo: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    UiRow(style = TodoRowStyle) {
        UiCheckbox(checked = todo.done, onCheckedChange = { onToggle() })
        UiBox(style = FlexFillStyle) {
            UiText(
                todo.text,
                style = if (todo.done) TodoTextDoneStyle else TodoTextActiveStyle,
            )
        }
        Button(onClick = onDelete, variant = ButtonVariant.Ghost) {
            UiText("×", style = DeleteIconStyle)
        }
    }
}

@Composable
private fun Footer(state: TodoState) {
    UiRow(style = FooterRowStyle) {
        UiRow(style = FooterBadgeRowStyle) {
            Badge { UiText("${state.remainingCount} left") }
            if (state.doneCount > 0) {
                Badge { UiText("${state.doneCount} done") }
            }
        }
        if (state.doneCount > 0) {
            Button(onClick = { state.clearCompleted() }, variant = ButtonVariant.Ghost) {
                UiText("Clear completed")
            }
        }
    }
}
