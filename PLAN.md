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
| `unicompose-base` | design system | Opinionated layer on top: `Tokens`, `TokenRefs`, `UnicomposeTheme`, `Card`, `Badge`, `Heading`, `H1`/`H2`/`H3`, `Button` (Primary/Secondary/Ghost), `TextField`. Themed widgets read from the active token set. Optional — consumers can swap with their own design library. |
| `unicompose-css-extractor` | build tool | Composite-build Kotlin compiler IR plugin + Gradle plugin. Walks IR for `Style(...)` calls during `compileKotlinJs`, emits a real `unicompose-generated.css` file, auto-wires into `jsProcessResources` so consumers just apply `id("dev.unicompose.css-extractor")`. |

This mirrors how the web actually works: the browser ships a minimal UA stylesheet and the styling mechanism (CSS); design systems like Material, Bootstrap, and Tailwind UI are layered on top by app authors. **`unicompose` is the styling mechanism + UA stylesheet equivalent. `unicompose-base` is one design system on top — replaceable.**

Each multi-target module uses Kotlin's `expect`/`actual` with these source sets:

- `commonMain` — public `expect` declarations + commonMain helpers.
- `composeAppMain` — Compose Multiplatform `actual`s, shared by `androidMain` + `iosMain`.
- `jsMain` — Compose HTML `actual`s.

### Mechanism-layer text inheritance

`unicompose` exposes a `LocalDefaultTextColor: CompositionLocal<Color?>` that `UiText` reads when `style.color` is null — CSS-like inheritance for the only typographic property that always cascades. Design libraries (and consumers building their own) write to this local at theme-provider time. Inheritance for `fontSize`/`fontWeight`/font family will be added when those properties have clear cross-platform semantics.

### Web styling: build-time atomic CSS extraction (StyleX-aligned)

Each unique `Style` is hashed into a deterministic class name. The Kotlin IR compiler plugin in `:unicompose-css-extractor` walks IR for `Style(...)` constructor calls during `compileKotlinJs`, evaluates literal arguments + token references, and emits hashed atomic CSS classes into `build/generated/css/unicompose-generated.css`. The runtime `AtomicCss` path stays as a graceful fallback for non-extractable dynamic styles — it produces identical hashes so the two paths share rules without duplication.

A static `unicompose-reset.css` (browser-default overrides — `box-sizing`, body font, `<h1>` margins, input accent-color) ships from the css-extractor plugin's resources and rides into the JS dist via `jsProcessResources`.

Cross-module aggregation works through Gradle "variants and attributes": each module that applies the css-extractor plugin exposes a consumable configuration tagged `Category=unicompose-extracted-css`. The same plugin's resolver auto-collects matching artifacts from every dep on the consumer's compile graph, merges them into one served `unicompose-generated.css`. Consumers add nothing beyond `id("dev.unicompose.css-extractor")`.

Theme-token references (`Color.token(TokenRefs.colors.accent)`, `Dp.token(TokenRefs.space.md)`, etc.) lower to CSS `var(--uc-...)` references in the generated file. `UnicomposeTheme` writes the active token values onto `<html>` as CSS custom properties. Theme switches become a handful of `setProperty` calls — no recomposition needed for the styled subtree.

### Canvas bundle gotchas (resolved)

Two CMP 1.10 wasmJs sharp edges we hit and worked around. Both are tracked here
because they will save the next person hours of bisection.

**1. `ComposeViewport(viewportContainer = document.body!!)` doesn't bootstrap.**
The recommended replacement for the deprecated `CanvasBasedWindow` looks right
on paper but in CMP 1.10 our wasmJs bundle's promise resolves to
`{ _initialize, memory }` and `main()` never runs — silent failure, no canvas.
The deprecated `CanvasBasedWindow(canvasElementId = "ComposeTarget")` works
end-to-end with the same setup. The
[canonical JetBrains template](https://github.com/Kotlin/kotlin-wasm-compose-template)
also still uses `CanvasBasedWindow`, which is what tipped us off. Fix: use
`CanvasBasedWindow` with `@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")`
until CMP smooths out the migration.

**2. Production wasm-opt strips Kotlin/Wasm init code.**
`wasmJsBrowserDistribution` (production) runs Binaryen `wasm-opt` against the
emitted wasm. The result loads cleanly but `main()` never runs — same symptom
as above, different cause. The development distribution
(`wasmJsBrowserDevelopmentExecutableDistribution`) skips wasm-opt and renders
correctly. Production is ~1.5 MB app-wasm, dev is ~19 MB. Fix: the `previewSite`
Gradle task uses the *development* distribution. For dev/preview/visual-test
infrastructure the size is acceptable; production-mode rendering on the web is
the *DOM* bundle's job anyway.

### Mobile preview via CMP-for-Web (canvas bundle)

The kitchen-sink sample produces **two web bundles** from the same `commonMain` `App()`:

| Bundle | Target | Renderer | Size | Use |
|---|---|---|---|---|
| `kitchen-sink-html.js` | `js(IR)` | Compose HTML → real `<span>`/`<div>`/`<button>` DOM | ~466 KB | Production web — SEO, accessibility, DOM interop. |
| `kitchen-sink-canvas.js` | `wasmJs` | Compose Multiplatform for Web → Skia canvas via WASM | ~28 MB total (8.2 MB Skiko + 19 MB code, dev distribution) | Dev preview, visual regression test against mobile. **Same Skia renderer that runs on iOS/Android**, so this bundle's output is pixel-equivalent to mobile (modulo system font fallback). |

The two targets use *different platform types* (`js` vs `wasmJs`) — Kotlin Multiplatform doesn't support two named JS targets in the same module, but `js` and `wasmJs` are distinct enough that they coexist cleanly. The canvas target joins `composeAppMain` in the source-set hierarchy so its rendering inherits the same CMP actuals that drive Android and iOS.

**Build infrastructure**:
- `./gradlew :samples:kitchen-sink:jsBrowserDistribution` → DOM bundle in `build/dist/js/productionExecutable/`.
- `./gradlew :samples:kitchen-sink:wasmJsBrowserDevelopmentExecutableDistribution` → canvas bundle in `build/dist/wasmJs/developmentExecutable/`. Production distribution (`wasmJsBrowserDistribution`) builds but produces a non-rendering bundle — see "Canvas bundle gotchas" above.
- `./gradlew :samples:kitchen-sink:previewSite` → both bundles copied into `build/dist/preview/{html,canvas}/`. Serve with `python3 -m http.server` from `preview/` and open `/html/index.html` or `/canvas/index.html`.

This setup is designed for Playwright A/B comparison testing: both bundles live at predictable subpaths (`/html/`, `/canvas/`), so a Playwright script can render the same App in both, capture screenshots, and diff them. The DOM bundle's render is the production output; the canvas bundle's render is the mobile output. The diff catches visual regressions on both backends from a single test harness.

Settings repos for WasmJs: `binaryen` releases from `github.com/WebAssembly/binaryen` (the wasm-opt toolchain) — added to `dependencyResolutionManagement.repositories` alongside the existing nodejs/yarn entries.

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
- `TokenRefs` exposes compile-time-resolvable string constants for every slot — `TokenRefs.colors.accent = "--uc-colors-accent"`, etc. Used at Style declaration sites via `Color.token(TokenRefs.colors.accent)`, `Dp.token(...)`, `Sp.token(...)`.
- `UnicomposeTheme(tokens = …)` provider sets the active theme via `LocalTokens`, propagates `tokens.colors.textPrimary` to `LocalInheritedText`, AND on the web writes every `--uc-*` CSS custom property onto `<html>` via direct DOM mutation.
- `LightTokens` and `DarkTokens` ship as defaults.
- Widget defaults are top-level `val`s (`CardStyle`, `ButtonPrimaryStyle`, `HeadingStyle`, `BadgeStyle`, `TextFieldLabelStyle`, etc.) using `Color.token` / `Dp.token` so the IR plugin extracts them statically. The legacy `*Defaults.style()` Composable accessors stay as backwards-compatible shims returning the same constants.
- On CMP: `Style.resolveRefs(tokens)` walks every typed slot (Color, Dp, Sp, plus nested in Border/Shadow/Padding/etc.) and resolves Refs to Literals against the active theme before the modifier chain sees them.

### vs StyleX

We're **StyleX-shaped, not StyleX-robust** (~5–10% of StyleX's surface). We have the typed style object, atomic CSS, and merging. We don't have pseudo-classes (`:hover`/`:focus`), media queries, container queries, dynamic styles, or keyframes. Some gaps are the cost of cross-platform (no clean Compose analog for `:hover`/`@media`); others are just under-built (per-side borders, gradients, shadows, design tokens).

## Widget set

### `unicompose` — primitives (unstyled, mechanism layer)

**No Material3 dependency** in the mechanism layer — all CMP actuals are foundation primitives so DOM and Skia render the same shape on both backends.

| Widget | Status | Web emits | CMP emits |
|---|---|---|---|
| `UiText` | shipped | `<span>` | foundation `BasicText` |
| `UiHeading` (H1/H2/H3) | shipped | `<h1>` / `<h2>` / `<h3>` | foundation `BasicText` w/ default heading style |
| `UiBox` | shipped | `<div display:flex>` | `Row` or `Column` |
| `UiRow` / `UiColumn` | shipped (commonMain wrappers) | — | — |
| `UiSpacer` | shipped (commonMain wrapper) | — | — |
| `UiDivider` | shipped | `<hr>` | thin `Box` |
| `UiButton` | shipped | `<button>` (browser defaults reset) | clickable `Box` |
| `UiCheckbox` | shipped | `<input type=checkbox>` | 14 dp `Box` w/ Canvas-drawn checkmark |
| `UiTextField` | shipped | `<input type=text>` | `BasicTextField` w/ thin-border `Box` + `BasicText` placeholder overlay |
| `UiLink` | shipped | `<a href>` | clickable styled `BasicText` (uses `LocalUriHandler`) |
| `UiSwitch` | shipped | `<input type=checkbox role=switch>` | rounded-pill track `Box` + circle thumb (no animation in v0.1) |
| `UiRadioGroup` | shipped | `<div role=radiogroup>` of `<input type=radio>` | `Column` of `Box`-based circle borders w/ filled inner circle |
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
| `Heading` (H1/H2/H3) | shipped | `UiHeading` + `HeadingDefaults.style()` (token color, level-default size/weight) |
| `Button` (Primary / Secondary / Ghost variants) | shipped | `UiButton` + `UiText` + `ButtonDefaults.style(variant)` (background, border, padding, radius, typography all token-driven) |
| `TextField` (with optional label) | shipped | `UiTextField` + label `UiText` (label typography from tokens; input chrome stays platform-native pending richer style hooks on the primitive) |

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
| `boxShadow` (offset + blur + spread + color) | shipped | hard shadow (blur=0) takes the `drawBehind` path with full offset+spread fidelity; blurred shadow uses `Modifier.shadow` elevation approximation (offset+spread ignored on CMP, full on web) |
| `lineHeight`, `letterSpacing`, `textAlign` | shipped | text properties read by `UiText` from the active `Style` |
| `transform` (translate / scale / rotate) | TODO | post-v0.1 |
| `transition` (duration + easing on a property set) | TODO | post-v0.1 — needs care since CMP uses `animate*AsState`, not declarative transitions |
| linear gradients (`backgroundGradient: LinearGradient?`) | shipped | `Brush.linearGradient` via `Modifier.background` on CMP; `background-image: linear-gradient(...)` on web. 8 directions; optional explicit color stops. Stays on the fast path — no custom drawing needed. |
| theme-token refs on every typed slot | shipped | `Color`, `Dp`, `Sp` are sealed interfaces with `Literal` + `Ref(cssVarName)` variants. `Color.token(TokenRefs.colors.accent)` / `Dp.token(...)` / `Sp.token(...)` slot into the same fields as literals. Refs lower to `var(--name)` on web; CMP resolves through the active theme via `Style.resolveRefs`. Border, Shadow, LinearGradient automatically support themes because they hold Color/Dp internally. |
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
- [x] **Shadow offset + spread (hard path)** — `Shadow(offsetX, offsetY, blur, spread, color)` matches CSS `box-shadow`. When `blur = 0` ("hard" shadow), CMP renders via `drawBehind` with full offset+spread fidelity. When `blur > 0`, the existing `Modifier.shadow` elevation approximation is used and offset/spread are ignored on CMP — full Skia mask-filter blurred shadows are deferred to a future custom-drawing pass.
- [x] **Linear gradients** — `LinearGradient(direction, colors, stops?)` for `Style.backgroundGradient`. Eight directions (4 axis-aligned + 4 diagonals), optional explicit color stops. Lowers to `Brush.linearGradient` on CMP and CSS `linear-gradient(...)` on web. Both built-in modifier paths — no drawing. Built on top of, not replacing, `backgroundColor`.
- [x] **`unicompose-base` themed widgets** — `Heading` wrapper (token color, level-default size/weight), `Button` with Primary/Secondary/Ghost variants (token-driven backgrounds, borders, padding, radius, typography), `TextField` with optional label. Each widget pairs with a namespaced `*Defaults` object exposing the resolved style for advanced consumers.
- [x] **Todo app sample** — single-screen todo app on all four targets (Android, iOS, JS DOM, wasmJs canvas). Form (TextField + Add Button), list (Card + Checkbox rows + Ghost delete), footer (Badges + Clear-completed Ghost button), and a dark-mode toggle exercising the theming layer. State held in a plain `mutableStateOf`/`SnapshotStateList` — persistence deferred until a follow-up needs it. Web bundles wired into the Playwright suite as a second visual-regression sample.
- [x] **Snapshot tests** — Two render backends covered:
    - **Web (DOM + Skia/wasmJs)** — Playwright suite at `tests/visual/`, kitchen-sink + todo-app each with html + canvas goldens. The wasmJs canvas bundle IS the iOS render path (same Skia backend, same StyleToModifier reducer), so this transitively covers the iOS rendering pipeline minus platform integration.
    - **Android (Compose/Skia)** — Paparazzi suite at `tests/snapshot-android/`. Two scenes (widget gallery + todo-list) × two themes (light + dark) = 4 goldens. Runs on the JVM via LayoutLib (no emulator). `verifyPaparazziDebug` is the CI gate; `recordPaparazziDebug` regenerates.
    - Native iOS goldens (XCUITest / swift-snapshot-testing) deferred to post-v0.1 — blocked on the Xcode-project scaffold and only adds value once iOS-only integration code exists. The Playwright canvas + Paparazzi pair already covers both render backends.
- [x] **API stability check** — Kotlinx Binary Compatibility Validator (0.18.0) wired in. Each published module has a committed `<module>.api` (JVM/Android signatures) and `<module>.klib.api` (iOS / JS / wasmJs ABI). `./gradlew apiCheck` (auto-wired into `check`) diffs against the committed manifests and fails on any unannounced public-API change. Samples are excluded.
- [x] **No Material3 dependency in the mechanism layer** — all CMP actuals (UiCheckbox, UiTextField, UiSwitch, UiRadioGroup, UiText, UiLink) rewritten as foundation primitives. Removed `implementation(compose.material3)` from `:unicompose` entirely. DOM and Skia now render the same shape on both backends; no more purple `colorScheme.primary` checkmarks bleeding through on Android/iOS/wasmJs canvas.
- [x] **Build-time CSS extraction (StyleX-aligned)** — new `:unicompose-css-extractor` composite-build module. Kotlin compiler IR plugin walks IR for `Style(...)` constructor calls during `compileKotlinJs`, evaluates literal/ref args via a constant Evaluator, and emits hashed atomic CSS classes (matching `AtomicCss`'s runtime format byte-for-byte) to `build/generated/css/unicompose-generated.css`. Companion Gradle plugin auto-wires reset.css + the IR-extractor output + cross-module aggregated CSS (via Gradle "variants and attributes" with `Category=unicompose-extracted-css`) into `jsProcessResources`. Consumers apply the plugin via `id("dev.unicompose.css-extractor")` and get everything in their JS dist for free.
- [x] **TokenRef pattern** — `Color`, `Dp`, `Sp` are sealed interfaces with `Literal` + `Ref(cssVarName)` variants and `.token(name)` factories. Eliminates ~9 parallel `*Ref` Style fields that an earlier iteration introduced; refs slot into the existing typed slots (`color`, `padding`, `border`, `boxShadow`, etc.). `UnicomposeTheme` on the web writes `--uc-*` CSS custom properties onto `<html>`, so theme switching is a handful of `setProperty` calls instead of recomposition. `Style.resolveRefs(tokens)` resolves refs to literals on CMP before the modifier chain sees them.
- [x] **Widget defaults migrated to top-level vals** — `CardStyle`, `ButtonPrimaryStyle`/`Secondary`/`Ghost` (built on `ButtonSharedStyle`), `HeadingStyle`, `BadgeStyle`, `TextFieldLabelStyle`, `TextFieldRowStyle`. All use `Color.token(...)` / `Dp.token(...)` / `Sp.token(...)` so the IR plugin extracts them. Backwards-compatible `*Defaults.style()` Composables remain. Coverage in samples: 9/9 sites in unicompose-base, 18/18 sites in samples/todo-app commonMain.

## Post-v0.1

- **Native iOS snapshot tests** — Once the Xcode project is scaffolded (separate TODO), add XCUITest or swift-snapshot-testing goldens to catch iOS-platform-specific drift (font metrics, status-bar insets, UIViewController integration). Lower priority than it sounds: the wasmJs canvas Playwright goldens already exercise the same Skia render pipeline, so the marginal coverage is the iOS-only platform glue, not the widget rendering itself.
- **Blurred shadow with offset/spread on CMP** — the hard-path shipped above covers `blur = 0`. Combining offset/spread with `blur > 0` on CMP needs platform-specific Skia mask-filter access (`BlurMaskFilter` on Android, Skiko `MaskFilter.makeBlur` on iOS via `paint.asFrameworkPaint().maskFilter`). Requires splitting `composeAppMain` into separate `androidMain`/`iosMain` paths for the helper. ~50 LOC of platform-specific code; defer until a real consumer needs it.
- **Radial / conic gradients** — natural follow-ups to linear gradients. `Brush.radialGradient` / `Brush.sweepGradient` exist on CMP; CSS has `radial-gradient(...)` / `conic-gradient(...)`. Same shape as the linear gradient implementation, just a sealed `Gradient` type.
- **Pseudo-states** (`:hover`, `:disabled`) — `StyleStates(base, hover, disabled)` shape; emit `:hover` selector on web and use `InteractionSource` on CMP.
- **`transform`, `transition`, gradients** — covered in the Style-surface table above.
- `UiImage` — pulls Coil3 (multiplatform image loading) for async network images on CMP. On web it's `<img src>`. Decide on placeholder/error API.
- `UiIcon` — needs a vector icon source. Options: SVG strings as resources, Material Symbols font, or inline SVG paths. Each has tradeoffs.
- `UiLazyColumn`/`UiLazyRow` — DOM virtualization is the hardest single piece.
- `UiModal`/`UiPopover`/`UiToast` — overlay rendering.
- `UiNavHost`/`UiNavLink` — wraps Compose Navigation 3 and the History API. **Load-bearing** for the "no bifurcation with Kobweb" story — see the README's framework-pairing notes.
- `UiModifier` escape hatch — typed wrapper for platform-specific extensions.
- Desktop (JVM) target — cheap to add since it shares the CMP backend.
- Dokka HTML doc site.
- **Publish to Maven** — replaces the current composite-build dev workflow with proper artifact resolution. Consumers would just add `pluginManagement.repositories { gradlePluginPortal() }` and apply `id("dev.unicompose.css-extractor")`. The composite-build setup is fine for in-repo dev but doesn't scale to external consumers.
- **iOS Xcode project scaffold** — currently `:samples:kitchen-sink:iosSimulatorArm64Test` etc. compile, but there's no Xcode project to actually launch on a simulator/device. Needs a minimal `iosApp/` SwiftUI host that calls `MainViewControllerKt.MainViewController()`. Blocks native iOS snapshot tests too.

### Bundled font for cross-platform visual parity

**What it would fix.** After all the typography-cascade work, three cosmetic
differences remain between the canvas and DOM web bundles (and therefore
between mobile and web in general):

  - Heading weight reads slightly heavier on canvas (Skia bundled font's
    "Bold" weight ≠ browser system font's "Bold" weight)
  - Letter spacing minor differences
  - Body text wraps at slightly different widths (different font advance-width
    tables)

All three trace to one root: Skia uses a bundled font (likely a Roboto-shaped
default); the browser uses a system font (San Francisco on macOS, Segoe UI on
Windows, etc.). `Style.fontFamily = FontFamily.Default` tells each platform
"use your default UI font" — semantically consistent but the actual font face
differs because each platform's default differs.

**Why it's currently deferred.** The remaining differences are the legitimate
mobile-vs-web rendering reality (canvas IS what mobile shows). Bundling a
font hides that truth in exchange for visual uniformity — a real product
decision that depends on whether brand consistency matters more than platform
consistency. Both are defensible.

**What would be needed.**
  - A new `unicompose-font-<name>` module per supported font (e.g.
    `unicompose-font-inter`).
  - Per-backend resource loading:
    - Web: `@font-face { src: url(...woff2...); }` shipped with the bundle.
    - Android: font in `res/font/` registered via `FontFamily(Font(R.font.x))`.
    - iOS: font registered with the system or loaded via Skia.
    - Canvas web (Skia): load the font into Skiko's typeface cache via
      `FontMgr.makeFromData(...)`.
  - A shared API: `Style(fontFamily = InterFontFamily)` — meaning a sealed
    `FontFamily` superset (Default / SansSerif / Serif / Monospace +
    `FontFamily.Custom(name)` or per-module concrete objects).

**Cost.** ~150 KB per bundle (woff2-compressed for Inter Regular + Bold +
Medium subset to Latin glyphs). Adds real engineering across four backend
loading paths and a versioning question (which font, which weights, which
subset).

**Recommendation when revisiting.** Start with Inter — clean modern sans-serif,
permissively licensed, well-supported, available in SIL OFL. Build
`unicompose-font-inter` as the proof-of-concept module. Once the pattern is
in place, additional fonts (Roboto, IBM Plex, etc.) become drop-in modules.

## Explicitly not planned

- `@media` queries — handled by the theming layer instead (different token sets per scheme).
- Container queries.
- Keyframe animations — use CMP's `animate*AsState` / `Animatable` directly; they're better than CSS keyframes for app UI.
- Server-side rendering on web.
- `RowReverse` / `ColumnReverse` — Compose has no native reverse for `Row`/`Column`; the LayoutDirection.Rtl workaround inverts text direction inside, which is worse than not supporting it.

## Verification

```bash
./gradlew assemble                                          # builds everything: Android APK, iOS frameworks, web JS+wasmJs bundles
./gradlew :samples:todo-app:installDebug                    # Android
./gradlew :samples:todo-app:jsBrowserDevelopmentRun         # localhost:8080, real DOM (with build-time CSS)
./gradlew visualPreview && cd build/tests-preview && python3 -m http.server 8000
                                                            # http://localhost:8000/todo-html/  + /todo-canvas/
./gradlew check                                             # apiCheck + Paparazzi + unit tests
cd tests/visual && npx playwright test                      # web goldens (DOM + Skia canvas, both samples)
# iOS: TODO scaffold iosApp/ Xcode project (post-v0.1)
```

The web bundle's view-source must show real HTML elements (`<div class="ucf-…">`, `<span class="uc-…">`), a `<link rel="stylesheet" href="unicompose-generated.css">` for the build-time-extracted classes, and a `<link rel="stylesheet" href="unicompose-reset.css">` for the static reset — not a `<canvas>`.
