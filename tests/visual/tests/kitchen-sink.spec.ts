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
   * Canvas bundle — mobile-equivalent preview via CMP-for-Web.
   *
   * SKIPPED until the wasmJs bundle init issue is resolved. Symptom: the
   * webpack-emitted bundle exposes only `{ _initialize, memory }` after
   * promise resolution — the Kotlin `main()` is never invoked, so
   * `ComposeViewport` never runs and no canvas is added to the DOM. With the
   * canonical `CanvasBasedWindow` setup we get one step further (canvas does
   * appear) but then a `TypeError: ef is not a function` from a wasm→JS
   * import. Both signal the same underlying class of issue: bundle
   * initialization in CMP 1.10 + Kotlin 2.2.20 wasmJs setup.
   *
   * Tracked in PLAN.md under "Known issues — canvas bundle".
   */
  test.skip('canvas bundle renders golden', async ({ page }) => {
    await page.goto('/canvas/index.html');
    await page.waitForFunction(
      () => document.querySelector('#ComposeTarget canvas') !== null,
      { timeout: 30_000 },
    );
    await page.waitForTimeout(1_000);
    await expect(page).toHaveScreenshot('kitchen-sink-canvas.png', {
      fullPage: true,
    });
  });

  /**
   * Side-by-side comparison view — DOM left, (currently empty) canvas right.
   * The DOM half is the production output; the canvas half will fill in once
   * the canvas bundle init is fixed. Worth keeping the test to lock the
   * comparison-page chrome itself.
   */
  test('comparison view renders golden', async ({ page }) => {
    await page.goto('/compare.html');
    await page.waitForLoadState('networkidle', { timeout: 30_000 });
    await page.waitForTimeout(1_500);
    await expect(page).toHaveScreenshot('kitchen-sink-comparison.png', {
      fullPage: true,
    });
  });
});
