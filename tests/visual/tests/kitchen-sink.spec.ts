import { test, expect } from '@playwright/test';

test.describe('kitchen-sink visual regression', () => {
  /**
   * DOM bundle — production web target. Loads fast; just wait for the React-
   * shaped Composable tree to hydrate into the #root div.
   */
  test('html bundle renders golden', async ({ page }) => {
    await page.goto('/html/index.html');
    await page.waitForSelector('#root > *', { timeout: 10_000 });
    // Brief settle for any fonts/CSS that load asynchronously.
    await page.waitForTimeout(500);
    await expect(page).toHaveScreenshot('kitchen-sink-html.png', {
      fullPage: true,
    });
  });

  /**
   * Canvas bundle — mobile-equivalent preview via CMP-for-Web. Loads ~10 MB of
   * WASM (Skia + the unicompose code), so the timeout is generous and we wait
   * both for the canvas element to appear AND for Skia to draw the first frame.
   */
  test('canvas bundle renders golden', async ({ page }) => {
    await page.goto('/canvas/index.html');
    // Skia bundle has to load ~10 MB of WASM (Skiko + the unicompose code),
    // initialize Compose, measure the layout, and draw the first frame.
    // 5 seconds is comfortably above measured cold-start in headless Chromium.
    await page.waitForSelector('canvas#ComposeTarget', { timeout: 30_000 });
    await page.waitForTimeout(5_000);
    await expect(page).toHaveScreenshot('kitchen-sink-canvas.png', {
      fullPage: true,
    });
  });

  // The previous side-by-side `compare.html` golden test was removed: it
  // captures a derived view (the same two bundles wrapped in iframes), so
  // the per-bundle goldens above already cover the visual regressions.
  // `compare.html` itself stays — it's produced by the `previewSite` Gradle
  // task for human / manual review. If we want a committed showcase image
  // for the README, generate it via a separate non-test capture task so
  // documentation drift doesn't show up as test failures.
});

