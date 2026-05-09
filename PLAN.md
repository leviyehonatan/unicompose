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

The codebase is split into three layers, mirroring how CSS itself is layered (UA stylesheet → CSS mechanism → author design system):

| Module | Layer | Role |
|---|---|---|
| `unicompose-style` | primitives | Pure-Kotlin `Style` data class + value types (`Dp`, `Sp`, `Color`, layout enums). No Compose dependency. |
| `unicompose` | mechanism + UA-stylesheet equivalents | The styling pipeline (atomic CSS, flex bridging, text-color inheritance) plus unstyled primitives (`UiText`, `UiBox`, `UiButton`, etc.). Minimal "looks reasonable on both platforms" defaults — no theme, no design opinions. |
| `unicompose-base` | design system | Opinionated layer on top: `Tokens`, `UnicomposeTheme`, `Card`, `Badge`. Themed widgets read from the active token set. Optional — consumers can swap with their own design library. |

This mirrors how the web actually works: the browser ships a minimal UA stylesheet and the styling mechanism (CSS); design systems like Material, Bootstrap, and Tailwind UI are layered on top by app authors. **`unicompose` is the styling mechanism + UA stylesheet equivalent. `unicompose-base` is one design system on top — replaceable.**

Each multi-target module uses Kotlin's `expect`/`actual` with these source sets:

- `commonMain` — public `expect` declarations + commonMain helpers.
- `composeAppMain` — Compose Multiplatform `actual`s, shared by `androidMain` + `iosMain`.
- `jsMain` — Compose HTML `actual`s.

### Mechanism-layer text inheritance

`unicompose` exposes a `LocalDefaultTextColor: CompositionLocal<Color?>` that `UiText` reads when `style.color` is null — CSS-like inheritance for the only typographic property that always cascades. Design libraries (and consumers building their own) write to this local at theme-provider time. Inheritance for `fontSize`/`fontWeight`/font family will be added when those properties have clear cross-platform semantics.

### Web styling: runtime atomic CSS

Each unique `Style` is hashed into a deterministic class name and registered into a singleton `<style id="unicompose-styles">` element on first use. Subsequent usages reuse the cached class. Identical observable behavior to a build-time KSP extractor (same DOM, same SEO/perf properties), with substantially less build complexity.

### CMP-side custom drawing

Most Style properties lower to a built-in `Modifier` (padding, background, clip, etc.). A few don't have a clean built-in equivalent — per-side border is the first; CSS-style shadows with offset and gradients are next. For these, the CMP backend drops into `Modifier.drawBehind { ... }` and paints to the Skia canvas directly.

The decision rule: **prefer a built-in modifier when one fits; reach for `drawBehind` only when the modifier surface is insufficient.** Custom drawing has real costs — it bypasses Compose's optimizations, doesn't compose well with elevation/blur effects, and is per-platform Skia code. The fast/slow split is exposed in the API where useful (e.g., `Border.isUniform` triggers the fast path).

### CMP-side flex bridging

A `LocalFlexParent` CompositionLocal (commonMain) plus `LocalRowScope`/`LocalColumnScope` (composeAppMain) thread parent layout intent to children, so:

- `Style.flex` becomes `RowScope.weight` / `ColumnScope.weight` on the captured parent scope.
- `alignItems = Stretch` propagates so children apply `fillMaxHeight` / `fillMaxWidth`.
- `Style.margin` is implemented via an outer `Box(Modifier.padding(margin))` since Compose has no native child-level margin.

### Design tokens / theming (in `unicompose-base`)

Tokens are an opinion layer, not a primitive — so they live in `unicompose-base`, not in the underlying mechanism. The model mirrors StyleX's `defineVars` / `createTheme`:

- `Tokens` data class declares the design surface (`accent`, `bgPage`, `bgSurface`, `bgSubtle`, `textPrimary`, `textSecondary`, `borderSubtle`, `error`, `success`, plus spacing/type/radius scales).
- `UnicomposeTheme(tokens = …)` provider sets the active theme via `LocalTokens` and propagates `tokens.colors.textPrimary` to the underlying `LocalDefaultTextColor`.
- `LightTokens` and `DarkTokens` ship as defaults.
- Widget defaults (`CardDefaults.style()`, `BadgeDefaults.style()`) are `@Composable` functions that read from `currentTokens()`.

CSS custom-property emission to `:root` is a planned follow-up — once shipped, atomic CSS rules can reference `var(--uc-accent)` and theme switches happen without recomposition. Mechanism doesn't change; the runtime gets faster.

### vs StyleX

We're **StyleX-shaped, not StyleX-robust** (~5–10% of StyleX's surface). We have the typed style object, atomic CSS, and merging. We don't have pseudo-classes (`:hover`/`:focus`), media queries, container queries, dynamic styles, or keyframes. Some gaps are the cost of cross-platform (no clean Compose analog for `:hover`/`@media`); others are just under-built (per-side borders, gradients, shadows, design tokens).

## Widget set

### `unicompose` — primitives (unstyled, mechanism layer)

| Widget | Status | Web emits | CMP emits |
|---|---|---|---|
| `UiText` | shipped | `<span>` | Material3 `Text` |
| `UiHeading` (H1/H2/H3) | shipped | `<h1>` / `<h2>` / `<h3>` | styled `Text` (UA-stylesheet sizes) |
| `UiBox` | shipped | `<div display:flex>` | `Row` or `Column` |
| `UiRow` / `UiColumn` | shipped (commonMain wrappers) | — | — |
| `UiSpacer` | shipped (commonMain wrapper) | — | — |
| `UiDivider` | shipped | `<hr>` | thin `Box` |
| `UiButton` | shipped | `<button>` (browser defaults reset) | clickable `Box` |
| `UiCheckbox` | shipped | `<input type=checkbox>` | Material3 `Checkbox` |
| `UiTextField` | shipped | `<input type=text>` | `OutlinedTextField` |
| `UiLink` | shipped | `<a href>` | clickable styled `Text` (uses `LocalUriHandler`) |
| `UiSwitch` | shipped | `<input type=checkbox role=switch>` | Material3 `Switch` |
| `UiRadioGroup` | shipped | `<div role=radiogroup>` of `<input type=radio>` | `Column` of Material3 `RadioButton` rows |
| `UiImage` | post-v0.1 | `<img>` | needs Coil3 — defer |
| `UiIcon` | post-v0.1 | inline SVG | needs vector source — defer |
| `UiLazyColumn` / `UiLazyRow` | post-v0.1 | DOM windowing via IntersectionObserver | `LazyColumn` / `LazyRow` |
| `UiModal` / `UiPopover` / `UiToast` | post-v0.1 | `<dialog>` / portal | `Dialog` / `ModalBottomSheet` |
| `UiNavHost` / `UiNavLink` | post-v0.1 | History API + Composable router | Compose Navigation 3 |

### `unicompose-base` — themed widgets (opinion layer)

| Widget | Status | Built from |
|---|---|---|
| `Card` | shipped | `UiBox` + `CardDefaults.style()` reading `Tokens` |
| `Badge` | shipped | `UiBox` + `UiText` + `BadgeDefaults.style()` reading `Tokens` |
| themed `Heading` wrapper | post-v0.1 | `UiHeading` + token-aware color/weight |
| themed inputs (TextField, Button) | post-v0.1 | underlying primitive + tokens |

## Style surface

| Property | Status | Notes |
|---|---|---|
| `padding`, `margin` | shipped | margin on CMP via outer-padding wrap |
| `backgroundColor`, `color`, `opacity` | shipped | |
| `fontSize`, `fontWeight` | shipped | |
| `borderRadius` (per-corner via `BorderRadius`) | shipped | uniform via `BorderRadius.all`; per-corner via the data class constructor |
| `flexDirection` (Row, Column) | shipped | reverse modes intentionally omitted |
| `alignItems` (Start/Center/End/Stretch) | shipped | Stretch propagates to children on CMP |
| `justifyContent` (incl. Space*) | shipped | |
| `gap` | shipped | |
| `width`, `height` (Fixed/Fill/Wrap/Fraction) | shipped | |
| `flex` | shipped | applies in flex-child context only |
| `border` (per-side via `Border` + `BorderEdge`) | shipped | uniform via `Border.all`; per-side via the named-arg constructor. Uniform takes the `Modifier.border` fast path; per-side drops to `Modifier.drawBehind` |
| `boxShadow` (blur + color) | shipped | offset-x/y omitted — `Modifier.shadow` ignores them; visually equivalent across backends, not pixel-equivalent |
| `lineHeight`, `letterSpacing`, `textAlign` | shipped | text properties read by `UiText` from the active `Style` |
| `transform` (translate / scale / rotate) | TODO | post-v0.1 |
| `transition` (duration + easing on a property set) | TODO | post-v0.1 — needs care since CMP uses `animate*AsState`, not declarative transitions |
| linear gradients | TODO | post-v0.1 |
| `:hover` / `:disabled` variants | TODO | post-v0.1 — see "Pseudo-states" below |
| `@media` queries | not planned | no clean Compose analog; use the theming layer instead |
| keyframes / animations | not planned | use CMP's `animate*AsState` directly |

### Pseudo-states

Two state variants would unblock a lot of UI work and have plausible cross-platform mappings:

- `:hover` — emit a `:hover` rule on web; on CMP, track via `InteractionSource.hoverInteractions` and switch the active `Style` at composition time.
- `:disabled` — emit a `:disabled` selector on web; on CMP, the widget already knows `enabled`, so dim or override the active style.

The proposed shape is `StyleStates(base, hover = …, disabled = …)`. `:focus` and `:active` are deferrable. None of this is in v0.1.

## Roadmap to v0.1

- [x] **Skeleton** — Gradle multi-target setup, "hello world" `UiText` on three platforms.
- [x] **Layout + Style** — `UiBox`/`UiRow`/`UiColumn`, full Style data class, atomic CSS, flex bridging.
- [x] **Widget set in `unicompose`** — Spacer, Divider, Heading, Button, Checkbox, TextField, Link, Switch, RadioGroup all shipped. Image and Icon deferred to post-v0.1.
- [x] **Three-layer split** — `unicompose-style` (primitives), `unicompose` (mechanism + UA-stylesheet primitives), `unicompose-base` (opinionated design system). `Card`/`Badge`/`Tokens`/`UnicomposeTheme` moved to design module. Mechanism-level `LocalDefaultTextColor` for CSS-like text-color inheritance.
- [x] **Style polish** — uniform `border`, `boxShadow` (blur + color), per-corner `borderRadius` via the new `BorderRadius` data class, `lineHeight`/`letterSpacing`/`textAlign`.
- [x] **Per-side `border` + custom-drawing infrastructure** — `Border` + `BorderEdge` types support per-side widths/colors; CMP drops to `Modifier.drawBehind` when edges differ. Foundation for shadow offset, gradients, and other future Style properties that don't map to built-in modifiers.
- [ ] **`unicompose-base` widgets** — themed `Heading` wrapper (token color), themed `Button`/`TextField` wrappers (token-driven defaults). The design library equivalent of Material 3.
- [ ] **Todo app sample** — multi-screen app on all three platforms. Forms, list, persistence (via shared KMP), exercises the theming layer with a dark-mode toggle. The v0.1 ship gate.
- [ ] **Snapshot tests** — Paparazzi (Android) + screenshot tests (iOS) + Playwright (web), kitchen-sink + todo-app golden screens. Light + dark variants per screen.
- [ ] **API stability check** — Kotlinx Binary Compatibility Validator on `unicompose-style`, `unicompose`, and `unicompose-base` public surfaces.

## Post-v0.1

- **Shadow offset / spread** — CSS `box-shadow` supports per-axis offset and spread; Compose `Modifier.shadow` is elevation-only and ignores both. Needs platform-specific Skia mask filter access (`BlurMaskFilter` on Android, Skiko `MaskFilter` on iOS) — non-trivial cross-platform glue. The custom-drawing infrastructure introduced for per-side borders is the foundation; this is the next consumer of it.
- **Linear gradients** — same situation as shadow offset; would build on the custom-drawing path. Web emits CSS gradients trivially; CMP needs `Modifier.background(brush = Brush.linearGradient(...))` plumbed through Style.
- **Pseudo-states** (`:hover`, `:disabled`) — `StyleStates(base, hover, disabled)` shape; emit `:hover` selector on web and use `InteractionSource` on CMP.
- **`transform`, `transition`, gradients** — covered in the Style-surface table above.
- `UiImage` — pulls Coil3 (multiplatform image loading) for async network images on CMP. On web it's `<img src>`. Decide on placeholder/error API.
- `UiIcon` — needs a vector icon source. Options: SVG strings as resources, Material Symbols font, or inline SVG paths. Each has tradeoffs.
- `UiLazyColumn`/`UiLazyRow` — DOM virtualization is the hardest single piece.
- `UiModal`/`UiPopover`/`UiToast` — overlay rendering.
- `UiNavHost`/`UiNavLink` — wraps Compose Navigation 3 and the History API. **Load-bearing** for the "no bifurcation with Kobweb" story — see the README's framework-pairing notes.
- `UiModifier` escape hatch — typed wrapper for platform-specific extensions.
- KSP build-time CSS extraction (only if cold-start becomes a real bottleneck).
- Desktop (JVM) target — cheap to add since it shares the CMP backend.
- Dokka HTML doc site.

## Explicitly not planned

- `@media` queries — handled by the theming layer instead (different token sets per scheme).
- Container queries.
- Keyframe animations — use CMP's `animate*AsState` / `Animatable` directly; they're better than CSS keyframes for app UI.
- Server-side rendering on web.
- `RowReverse` / `ColumnReverse` — Compose has no native reverse for `Row`/`Column`; the LayoutDirection.Rtl workaround inverts text direction inside, which is worse than not supporting it.

## Verification

```bash
./gradlew assemble        # builds everything: Android APK, iOS frameworks, web JS bundle
./gradlew :samples:kitchen-sink:installDebug                # Android
./gradlew :samples:kitchen-sink:jsBrowserDevelopmentRun     # localhost:8080, real DOM
# iOS: open samples/kitchen-sink/iosApp in Xcode (TODO: scaffold the Xcode project)
```

The web bundle's view-source must show real HTML elements (`<div class="ucf-…">`, `<span class="uc-…">`) and a single `<style id="unicompose-styles">` block — not a `<canvas>`.
