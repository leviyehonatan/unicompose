package dev.unicompose.sample

import org.jetbrains.compose.web.renderComposable

public fun main() {
    renderComposable(rootElementId = "root") {
        App()
    }
}
