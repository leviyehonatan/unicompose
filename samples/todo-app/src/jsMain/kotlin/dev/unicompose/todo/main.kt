package dev.unicompose.todo

import org.jetbrains.compose.web.renderComposable

public fun main() {
    renderComposable(rootElementId = "root") {
        App()
    }
}
