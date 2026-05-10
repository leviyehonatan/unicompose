@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package dev.unicompose.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

/**
 * Entry point for the canvas-rendered web bundle (`kitchen-sink-canvas.js`).
 *
 * Renders the same `App()` composable from `commonMain` through Compose
 * Multiplatform for Web — the same Skia renderer that runs on iOS and Android
 * via Compose Multiplatform. The result is intended to be pixel-equivalent to
 * the mobile output, making this bundle a fast emulator-free way to preview
 * mobile rendering in a browser.
 *
 * Uses `CanvasBasedWindow` (deprecated) rather than the suggested replacement
 * `ComposeViewport`. In CMP 1.10 we bisected an issue where
 * `ComposeViewport(viewportContainer = document.body)` produces a bundle whose
 * promise resolves to `{ _initialize, memory }` and never invokes `main()` —
 * canvas never appears, no errors. The deprecated `CanvasBasedWindow` works
 * end-to-end with the same setup. The canonical
 * [JetBrains kotlin-wasm-compose-template](https://github.com/Kotlin/kotlin-wasm-compose-template)
 * also still uses `CanvasBasedWindow`, so this is a known-good path until CMP
 * smooths out the `ComposeViewport` migration.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
