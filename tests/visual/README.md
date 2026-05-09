# Visual regression tests

Playwright golden-screenshot tests for the kitchen-sink sample's web bundles.

Both web bundles are tested:
- **HTML** (`/html/index.html`) — production web target, real DOM via Compose HTML.
- **Canvas** (`/canvas/index.html`) — mobile-equivalent preview via CMP-for-Web (Skia).

A third test captures the **side-by-side comparison view** (`/compare.html`) for
human review and documentation.

Goldens live in `tests/__screenshots__/` and are committed to git so CI can
detect regressions. Visual diffs use a 1% pixel tolerance to absorb antialiasing
and font-rendering variation across runs.

## Run

From the repo root:

```sh
./gradlew :samples:kitchen-sink:visualTest          # check against goldens
./gradlew :samples:kitchen-sink:visualTestUpdate    # regenerate goldens
```

The `visualTest` task depends on `:samples:kitchen-sink:previewSite`, so both
bundles are rebuilt before tests run.

First-time setup needs Node + Playwright browsers; the Gradle task handles it
via `npm install` and `npx playwright install chromium`.

## Direct npm/Playwright usage

Sometimes faster for development:

```sh
cd tests/visual
npm install
npx playwright install chromium
npx playwright test
npx playwright show-report
```
