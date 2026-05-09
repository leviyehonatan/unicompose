# unicompose — plan

A small, deliberately-constrained UI primitive set for Kotlin that renders the same `@Composable` code on **Android, iOS, and real DOM web**. Inspired by [React Strict DOM](https://facebook.github.io/react-strict-dom/). Apache 2.0.

## Why

Cross-platform Kotlin UI today forces a hard pick:

- **Compose Multiplatform for Web** renders to a single `<canvas>` via Skia. Same Composables on Android/iOS/web, but the web output isn't real HTML — no SEO, weak accessibility, no DOM interop.
- **Compose HTML** renders real DOM, but its widget API (`Div`, `Span`, `H1`, CSS DSL) doesn't share with Android/iOS UI at all.

`unicompose` fills the gap: typed `Style` + small primitive set, two backends.

## Goals

- One `@Composable` tree → three rendered outputs (Android, iOS, real DOM).
- ~20 widgets covering the boring 80% of an app — forms, lists, navigation, modals.
- Web output is real HTML with semantic elements, working SEO, screen-reader-accessible.
- Typed `Style` data class as the styling primary; `UiModifier` escape hatch (post-v0.1).

## Non-goals

- Custom drawing (`Canvas`, `drawBehind`, `graphicsLayer`).
- Custom `Layout` blocks. Layout is **flexbox-only**.
- Pixel-perfect parity across backends — visual *equivalence* is the bar.
- Animation system beyond simple transitions (post-v0.1).
- Server-side rendering.
- Replacing Compose Multiplatform — `unicompose` is a constrained subset, not a superset.

## Architecture

Single multi-target Gradle module (`unicompose`) — Kotlin `expect`/`actual` requires both sides in the same module. Source sets:

- `commonMain` — public `expect` declarations + commonMain helpers.
- `composeAppMain` — Compose Multiplatform `actual`s, shared by `androidMain` + `iosMain`.
- `jsMain` — Compose HTML `actual`s.

Auxiliary module `unicompose-style` is pure-Kotlin (no Compose dep) and holds the `Style` data class + primitives (`Dp`, `Sp`, `Color`, layout enums).

### Web styling: runtime atomic CSS

Each unique `Style` is hashed into a deterministic class name and registered into a singleton `<style id="unicompose-styles">` element on first use. Subsequent usages reuse the cached class. Identical observable behavior to a build-time KSP extractor (same DOM, same SEO/perf properties), with substantially less build complexity.

### CMP-side flex bridging

A `LocalFlexParent` CompositionLocal (commonMain) plus `LocalRowScope`/`LocalColumnScope` (composeAppMain) thread parent layout intent to children, so:

- `Style.flex` becomes `RowScope.weight` / `ColumnScope.weight` on the captured parent scope.
- `alignItems = Stretch` propagates so children apply `fillMaxHeight` / `fillMaxWidth`.
- `Style.margin` is implemented via an outer `Box(Modifier.padding(margin))` since Compose has no native child-level margin.

## Widget set

| Widget | Status | Web emits | CMP emits |
|---|---|---|---|
| `UiText` | shipped | `<span>` | Material3 `Text` |
| `UiHeading` (H1/H2/H3) | shipped | `<h1>` / `<h2>` / `<h3>` | styled `Text` |
| `UiBox` | shipped | `<div display:flex>` | `Row` or `Column` |
| `UiRow` / `UiColumn` | shipped (commonMain wrappers) | — | — |
| `UiSpacer` | shipped (commonMain wrapper) | — | — |
| `UiDivider` | shipped | `<hr>` | thin `Box` |
| `UiCard` | shipped (commonMain wrapper) | — | — |
| `UiButton` | shipped | `<button>` | clickable `Box` |
| `UiCheckbox` | shipped | `<input type=checkbox>` | Material3 `Checkbox` |
| `UiTextField` | shipped | `<input type=text>` | `OutlinedTextField` |
| `UiLink` | TODO | `<a href>` | clickable styled `Text` |
| `UiImage` | TODO | `<img>` | `AsyncImage` (Coil3) |
| `UiSwitch` | TODO | `<input type=checkbox role=switch>` | Material3 `Switch` |
| `UiRadioGroup` | TODO | `<input type=radio>` | Material3 `RadioButton` |
| `UiBadge` | TODO | `<span class>` | Material3 `Badge` |
| `UiIcon` | TODO | inline SVG | `Icon` |
| `UiLazyColumn` / `UiLazyRow` | post-v0.1 | DOM windowing via IntersectionObserver | `LazyColumn` / `LazyRow` |
| `UiModal` / `UiPopover` / `UiToast` | post-v0.1 | `<dialog>` / portal | `Dialog` / `ModalBottomSheet` |
| `UiNavHost` / `UiNavLink` | post-v0.1 | History API + Composable router | Compose Navigation 3 |

## Style surface

| Property | Status | Notes |
|---|---|---|
| `padding`, `margin` | shipped | margin on CMP via outer-padding wrap |
| `backgroundColor`, `color`, `opacity` | shipped | |
| `fontSize`, `fontWeight` | shipped | |
| `borderRadius` | shipped | uniform corners only |
| `flexDirection` (Row, Column) | shipped | reverse modes intentionally omitted |
| `alignItems` (Start/Center/End/Stretch) | shipped | Stretch propagates to children on CMP |
| `justifyContent` (incl. Space*) | shipped | |
| `gap` | shipped | |
| `width`, `height` (Fixed/Fill/Wrap/Fraction) | shipped | |
| `flex` | shipped | applies in flex-child context only |
| `border` (color + width) | TODO | post-v0.1 |
| `boxShadow` | TODO | post-v0.1 |
| per-side border radii | TODO | post-v0.1 |

## Roadmap to v0.1

- [x] **Skeleton** — Gradle multi-target setup, "hello world" `UiText` on three platforms.
- [x] **Layout + Style** — `UiBox`/`UiRow`/`UiColumn`, full Style data class, atomic CSS, flex bridging.
- [ ] **Widget set** — finish the table above through `UiBadge`/`UiIcon`. ~6 widgets remaining.
- [ ] **Todo app sample** — multi-screen app on all three platforms. Forms, list, persistence (via shared KMP). The v0.1 ship gate.
- [ ] **Snapshot tests** — Paparazzi (Android) + screenshot tests (iOS) + Playwright (web), kitchen-sink + todo-app golden screens.
- [ ] **API stability check** — Kotlinx Binary Compatibility Validator on `unicompose-style` + public surface of `unicompose`.

## Post-v0.1

- `UiLazyColumn`/`UiLazyRow` — DOM virtualization is the hardest single piece.
- `UiModal`/`UiPopover`/`UiToast` — overlay rendering.
- `UiNavHost`/`UiNavLink` — wraps Compose Navigation 3 and the History API.
- `UiModifier` escape hatch — typed wrapper for platform-specific extensions.
- `border`, `boxShadow`, per-side radii.
- KSP build-time CSS extraction (only if cold-start becomes a real bottleneck).
- Desktop (JVM) target — cheap to add since it shares the CMP backend.
- Dokka HTML doc site.

## Verification

```bash
./gradlew assemble        # builds everything: Android APK, iOS frameworks, web JS bundle
./gradlew :samples:kitchen-sink:installDebug                # Android
./gradlew :samples:kitchen-sink:jsBrowserDevelopmentRun     # localhost:8080, real DOM
# iOS: open samples/kitchen-sink/iosApp in Xcode (TODO: scaffold the Xcode project)
```

The web bundle's view-source must show real HTML elements (`<div class="ucf-…">`, `<span class="uc-…">`) and a single `<style id="unicompose-styles">` block — not a `<canvas>`.
