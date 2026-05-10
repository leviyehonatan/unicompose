package dev.unicompose.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.unicompose.UiBox
import dev.unicompose.UiButton
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
import dev.unicompose.base.UnicomposeTheme
import dev.unicompose.base.currentTokens
import dev.unicompose.style.Align
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Justify
import dev.unicompose.style.Padding
import dev.unicompose.style.Size
import dev.unicompose.style.Style

/**
 * Single-screen todo app. Exercises:
 *  - UnicomposeTheme + dark-mode toggle (LightTokens / DarkTokens swap)
 *  - H1 from unicompose-base
 *  - Card and Badge themed widgets
 *  - Button (Primary / Ghost variants)
 *  - UiCheckbox, UiTextField (stateful input primitives)
 *  - Layout primitives (UiBox, UiRow, UiColumn) with gap, justify, align
 *  - Token-driven spacing, typography, colors throughout
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

@Composable
private fun Page(state: TodoState, isDark: Boolean, toggleDark: () -> Unit) {
    val t = currentTokens()
    UiColumn(
        style = Style(
            backgroundColor = t.colors.bgPage,
            padding = Padding.all(t.space.lg),
            gap = t.space.lg,
            width = Size.FillParent,
            height = Size.FillParent,
            alignItems = Align.Stretch,
        ),
    ) {
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
    val t = currentTokens()
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
        Button(onClick = toggleDark, variant = ButtonVariant.Secondary) {
            UiText(if (isDark) "Light" else "Dark")
        }
    }
}

@Composable
private fun AddTodoForm(state: TodoState) {
    val t = currentTokens()
    Card {
        UiRow(style = Style(gap = t.space.sm, width = Size.FillParent, alignItems = Align.Center)) {
            UiBox(style = Style(flex = 1f)) {
                UiTextField(
                    value = state.draft,
                    onValueChange = { state.draft = it },
                    placeholder = "What needs doing?",
                )
            }
            Button(onClick = { state.addDraft() }) {
                UiText("Add")
            }
        }
    }
}

@Composable
private fun TodoList(state: TodoState) {
    val t = currentTokens()
    if (state.items.isEmpty()) {
        Card {
            UiText(
                "Your list is empty — add something above.",
                style = Style(color = t.colors.textSecondary),
            )
        }
        return
    }
    Card {
        UiColumn(style = Style(width = Size.FillParent, gap = t.space.sm)) {
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
    val t = currentTokens()
    UiRow(
        style = Style(
            gap = t.space.sm,
            alignItems = Align.Center,
            width = Size.FillParent,
        ),
    ) {
        UiCheckbox(checked = todo.done, onCheckedChange = { onToggle() })
        UiBox(style = Style(flex = 1f)) {
            UiText(
                todo.text,
                style = Style(
                    color = if (todo.done) t.colors.textSecondary else t.colors.textPrimary,
                ),
            )
        }
        Button(onClick = onDelete, variant = ButtonVariant.Ghost) {
            UiText("×", style = Style(fontSize = t.type.lg, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun Footer(state: TodoState) {
    val t = currentTokens()
    UiRow(
        style = Style(
            justifyContent = Justify.SpaceBetween,
            alignItems = Align.Center,
            width = Size.FillParent,
            gap = t.space.sm,
        ),
    ) {
        UiRow(style = Style(gap = t.space.sm, alignItems = Align.Center)) {
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
