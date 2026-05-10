@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package dev.unicompose.todo

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

// CanvasBasedWindow over ComposeViewport — see kitchen-sink wasmJsMain/main.kt
// for the gory background. Same wasmJs init bug we worked around there.
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
