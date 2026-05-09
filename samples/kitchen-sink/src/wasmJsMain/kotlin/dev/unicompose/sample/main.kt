package dev.unicompose.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/**
 * Entry point for the canvas-rendered web bundle (`kitchen-sink-canvas.js`).
 *
 * Renders the same `App()` composable from `commonMain` through Compose
 * Multiplatform for Web — the *same Skia renderer* that runs on iOS and
 * Android via Compose Multiplatform. The result is pixel-equivalent to the
 * mobile output (modulo system font fallback), making this bundle a fast,
 * emulator-free way to preview mobile rendering in a browser.
 *
 * The DOM bundle (`kitchen-sink-html.js`) is the production web target; this
 * one is dev / preview / visual-regression-test infrastructure.
 */
@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        App()
    }
}
