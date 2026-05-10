package dev.unicompose.todo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

/**
 * One todo item. Stable [id] lets the UI key list iteration without relying on
 * text content (which mutates as the user edits).
 */
public data class Todo(
    val id: Int,
    val text: String,
    val done: Boolean = false,
)

/**
 * In-memory todo list state. Persistence (Multiplatform-Settings, JSON file,
 * or a real KMP DB) is a follow-up — see PLAN.md. For v0.1 the sample's value
 * is exercising the widget set + theming layer, not infrastructure.
 *
 * The list is a [SnapshotStateList] so individual mutations notify the
 * Composable layer without recreating the list reference.
 */
public class TodoState(initial: List<Todo> = SeedTodos) {
    private var nextId: Int = (initial.maxOfOrNull { it.id } ?: 0) + 1

    public val items: SnapshotStateList<Todo> = initial.toMutableStateList()

    public var draft: String by mutableStateOf("")

    public fun addDraft() {
        val text = draft.trim()
        if (text.isEmpty()) return
        items += Todo(id = nextId++, text = text)
        draft = ""
    }

    public fun toggle(id: Int) {
        val i = items.indexOfFirst { it.id == id }
        if (i >= 0) items[i] = items[i].copy(done = !items[i].done)
    }

    public fun delete(id: Int) {
        items.removeAll { it.id == id }
    }

    public fun clearCompleted() {
        items.removeAll { it.done }
    }

    public val remainingCount: Int get() = items.count { !it.done }
    public val doneCount: Int get() = items.count { it.done }
}

private val SeedTodos = listOf(
    Todo(1, "Try the unicompose dark-mode toggle"),
    Todo(2, "Tap a todo's checkbox", done = true),
    Todo(3, "Add a new item below"),
)
