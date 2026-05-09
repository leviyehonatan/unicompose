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

### Design tokens / theming (planned, pre-v0.1)

Per-component defaults (`DefaultCardStyle`, `DefaultBadgeStyle`, etc.) live in their respective widget files today. The styling system itself has **no design-token layer** — colors, sizes, and typography are baked into call-site values or per-widget defaults. That's the biggest pre-v0.1 gap.

The planned model mirrors StyleX's `defineVars` / `createTheme`:

- A `Tokens` data class declares the design surface (`accent`, `bgSurface`, `textPrimary`, `borderSubtle`, spacing scale, type scale, …).
- A `UnicomposeTheme(tokens = …)` provider sets the active theme.
- On Compose Multiplatform, tokens are exposed via a `CompositionLocal<Tokens>`.
- On the web, tokens are emitted as CSS custom properties on the document root; atomic CSS rules reference `var(--uc-accent)` etc. Theme switching becomes a single attribute change without reflowing every styled element.

Without this layer, theming requires forking widgets and consumers can't ship light/dark mode or brand customization.

### vs StyleX

We're **StyleX-shaped, not StyleX-robust** (~5–10% of StyleX's surface). We have the typed style object, atomic CSS, and merging. We don't have pseudo-classes (`:hover`/`:focus`), media queries, container queries, dynamic styles, or keyframes. Some gaps are the cost of cross-platform (no clean Compose analog for `:hover`/`@media`); others are just under-built (per-side borders, gradients, shadows, design tokens).

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
| `UiLink` | shipped | `<a href>` | clickable styled `Text` (uses `LocalUriHandler`) |
| `UiSwitch` | shipped | `<input type=checkbox role=switch>` | Material3 `Switch` |
| `UiBadge` | shipped (commonMain wrapper) | — | — |
| `UiRadioGroup` | shipped | `<div role=radiogroup>` of `<input type=radio>` | `Column` of Material3 `RadioButton` rows |
| `UiImage` | post-v0.1 | `<img>` | needs Coil3 — defer |
| `UiIcon` | post-v0.1 | inline SVG | needs vector source — defer |
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
| `border` (color + width, per-side) | TODO | pre-v0.1 — easy on both backends |
| `boxShadow` (single layer) | TODO | pre-v0.1 — common need; maps cleanly |
| per-corner border radii | TODO | pre-v0.1 — RoundedCornerShape on CMP, border-radius shorthand on CSS |
| `lineHeight`, `letterSpacing`, `textAlign` | TODO | pre-v0.1 — typography polish |
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
- [x] **Widget set** — Spacer, Divider, Heading, Card, Button, Checkbox, TextField, Link, Switch, Badge, RadioGroup all shipped. Image and Icon deferred to post-v0.1 (need external deps).
- [ ] **Theming / design tokens** — `Tokens` data class, `UnicomposeTheme` provider, CSS custom-property emission on web, `CompositionLocal<Tokens>` on CMP. Light + dark token sets shipped as defaults. **Highest-leverage missing piece** — every widget that exists today gets re-touched to read tokens instead of literals. Should land before the todo-app sample so the sample exercises the theme path.
- [ ] **Style polish** — `border` (color + width, per-side), `boxShadow` (single layer), per-corner `borderRadius`, `lineHeight`/`letterSpacing`/`textAlign`. All map cleanly both ways; deferred only because they weren't blocking. Land before snapshot tests so the goldens reflect realistic styling.
- [ ] **Todo app sample** — multi-screen app on all three platforms. Forms, list, persistence (via shared KMP), exercises the theming layer with a dark-mode toggle. The v0.1 ship gate.
- [ ] **Snapshot tests** — Paparazzi (Android) + screenshot tests (iOS) + Playwright (web), kitchen-sink + todo-app golden screens. Light + dark variants per screen.
- [ ] **API stability check** — Kotlinx Binary Compatibility Validator on `unicompose-style` + public surface of `unicompose`.

## Post-v0.1

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
