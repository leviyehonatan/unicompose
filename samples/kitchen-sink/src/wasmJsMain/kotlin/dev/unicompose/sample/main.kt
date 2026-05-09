package dev.unicompose.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/**
 * Entry point for the canvas-rendered web bundle (`kitchen-sink-canvas.js`).
 *
 * Renders the same `App()` composable from `commonMain` through Compose
 * Multiplatform for Web — the same Skia renderer that runs on iOS and Android
 * via Compose Multiplatform. The result is intended to be pixel-equivalent to
 * the mobile output, making this bundle a fast emulator-free way to preview
 * mobile rendering in a browser.
 *
 * KNOWN ISSUE — runtime init failure: as of Kotlin 2.2.20 + CMP 1.10 + our
 * dual-target build, the webpack-emitted bundle's promise resolves to
 * `{ _initialize, memory }` and `main()` is never invoked, so this composable
 * never runs. The bundle compiles and links cleanly; the failure is at runtime.
 * See PLAN.md "Known issues — canvas bundle" and tests/visual/ for the skipped
 * Playwright golden.
 */
@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    ComposeViewport(viewportContainer = document.body!!) {
        App()
    }
}
