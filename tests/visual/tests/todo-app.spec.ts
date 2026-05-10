import { test, expect } from '@playwright/test';

test.describe('todo-app visual regression', () => {
  test('html bundle renders golden', async ({ page }) => {
    await page.goto('/todo-html/index.html');
    await page.waitForSelector('#root > *', { timeout: 10_000 });
    await page.waitForTimeout(500);
    await expect(page).toHaveScreenshot('todo-app-html.png', { fullPage: true });
  });

  test('canvas bundle renders golden', async ({ page }) => {
    await page.goto('/todo-canvas/index.html');
    await page.waitForSelector('canvas#ComposeTarget', { timeout: 30_000 });
    await page.waitForTimeout(5_000);
    await expect(page).toHaveScreenshot('todo-app-canvas.png', { fullPage: true });
  });
});
