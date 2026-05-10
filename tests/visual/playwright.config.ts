import { defineConfig } from '@playwright/test';
import path from 'path';

// Serve the consolidated preview dir produced by `./gradlew visualPreview`.
// Layout under build/tests-preview/:
//   /html, /canvas, /compare.html      — kitchen-sink
//   /todo-html, /todo-canvas, /todo-compare.html  — todo-app
const PREVIEW_DIR = path.resolve(__dirname, '../../build/tests-preview');

export default defineConfig({
  testDir: './tests',
  // Fixed viewport so screenshots are deterministic across machines.
  use: {
    baseURL: 'http://127.0.0.1:4173',
    viewport: { width: 1024, height: 768 },
  },
  // Tolerance for visual diffs — the canvas bundle in particular shows tiny
  // sub-pixel differences across runs (font-rendering, antialiasing). 1% pixel
  // tolerance catches real regressions without false positives.
  expect: {
    toHaveScreenshot: {
      maxDiffPixelRatio: 0.01,
    },
  },
  // The canvas bundle has to load ~10 MB of WASM (Skiko + the unicompose code).
  // Per-test timeout is set to allow that comfortably.
  timeout: 60_000,
  // Bring up a static server before tests; tear it down after.
  // http-server (npm) handles IPv4/IPv6 dual stack cleanly and stays in the
  // node toolchain we already need for Playwright itself.
  webServer: {
    command: `npx http-server "${PREVIEW_DIR}" -p 4173 -s -c-1 -a 127.0.0.1`,
    url: 'http://127.0.0.1:4173/compare.html',
    timeout: 30_000,
    reuseExistingServer: !process.env.CI,
  },
  projects: [
    { name: 'chromium', use: { browserName: 'chromium' } },
  ],
  // Goldens live alongside the spec files, in __screenshots__/<spec>-<test>/<image>.png.
  // Committed to git so CI can compare against them.
  snapshotPathTemplate: '{testDir}/__screenshots__/{testFilePath}/{arg}{ext}',
});
