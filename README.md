# unicompose

Write one Composable, render it on Android, iOS, **real HTML/DOM**, and Skia-canvas web. Inspired by [React Strict DOM](https://facebook.github.io/react-strict-dom/). Apache 2.0.

```kotlin
@Composable
fun Greeting(name: String) {
    UiText("Hello, $name", style = Style(fontSize = 18.sp, color = Color.Black))
}
```

That same `Greeting` becomes `Text` in Jetpack Compose on Android/iOS, a Skia-rendered text run on canvas web, and a `<span>` element in real DOM web — accessible, indexable, semantic.

> **Status: pre-1.0**, v0.1 nearing tag. APIs may shift; we won't shy away from breaking changes until the public surface settles.

## What's special

- **Two web backends, your choice.** Production web → real DOM. Mobile preview / pixel-perfect parity → Skia canvas. Both render the same Composable code.
- **Build-time CSS extraction** for the DOM target — Composable-defined styles turn into a real `unicompose-generated.css` file at compile time via a Kotlin IR compiler plugin. StyleX-aligned architecture. Theming becomes "rewrite a few CSS variables on `<html>`," no recomposition needed.
- **Three layers, opt in to what you need.**
  - `:unicompose-style` — `Style` / `Color` / `Dp` / `Sp` data classes (pure Kotlin, no Compose dep).
  - `:unicompose` — UA-equivalent unstyled primitives: `UiBox`, `UiText`, `UiButton`, `UiCheckbox`, `UiTextField`, `UiSwitch`, `UiRadioGroup`, `UiHeading`, `UiLink`, `UiDivider`. No Material3 dependency.
  - `:unicompose-base` — opinionated themed widgets on top: `Card`, `Button` (Primary/Secondary/Ghost), `Heading` (`H1`/`H2`/`H3`), `Badge`, `TextField`, plus `Tokens` + `UnicomposeTheme`.

## Try it

The included samples build for every target:

```bash
# Web preview (DOM + Skia canvas, served side-by-side)
./gradlew visualPreview
cd build/tests-preview && python3 -m http.server 8000
# open http://localhost:8000/todo-html/    (real DOM)
# open http://localhost:8000/todo-canvas/  (Skia)

# Android: open in Android Studio, run :samples:todo-app
# iOS:     ./gradlew :samples:todo-app:wasmJsBrowserDevelopmentExecutableDistribution (Xcode scaffold TBD)
```

## Using it in a project

unicompose targets Kotlin Multiplatform with `js(IR)` (DOM web), `wasmJs` (canvas web), Android, and iOS targets. Apply the plugin and add the modules you want to your `commonMain`.

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("path/to/unicompose/unicompose-css-extractor")
    repositories { gradlePluginPortal(); mavenCentral() }
}
includeBuild("path/to/unicompose/unicompose-css-extractor")  // dep substitution
```

```kotlin
// app/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
    id("dev.unicompose.css-extractor")  // build-time CSS extraction for the JS target
}

kotlin {
    js(IR) { browser() }       // real DOM
    wasmJs { browser() }       // Skia canvas (mobile preview / parity)
    androidTarget()
    iosArm64(); iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("dev.unicompose:unicompose:0.1.0-SNAPSHOT")
            implementation("dev.unicompose:unicompose-base:0.1.0-SNAPSHOT")
            implementation(compose.runtime)
        }
    }
}
```

Once published. For now, depend via the included build (see `samples/todo-app/build.gradle.kts` for the working pattern).

## Authoring a screen

```kotlin
@Composable
fun App() {
    var dark by remember { mutableStateOf(false) }
    UnicomposeTheme(tokens = if (dark) DarkTokens else LightTokens) {
        UiColumn(style = PageStyle) {
            Header(isDark = dark, toggleDark = { dark = !dark })
            Card { Text("Hello from unicompose.") }
            Button(onClick = { /* ... */ }) { Text("Save") }
        }
    }
}

// Top-level Style declarations — extracted at compile time into the
// generated CSS, ride into the JS bundle automatically.
private val PageStyle = Style(
    backgroundColor = Color.token(TokenRefs.colors.bgPage),
    padding = Padding.all(Dp.token(TokenRefs.space.lg)),
    gap = Dp.token(TokenRefs.space.md),
    width = Size.FillParent,
    alignItems = Align.Stretch,
)
```

The same `App()` runs on every backend. Theme switching on the web rewrites a handful of `--uc-*` CSS variables on `<html>`; classes don't change.

### Style: literal vs token-ref

Every typed Style value (`Color`, `Dp`, `Sp`) has two variants: a literal and a theme-token ref.

```kotlin
Style(
    color = Color.White,                                  // literal
    backgroundColor = Color.token(TokenRefs.colors.bgPage), // ref → CSS var on web, resolved on CMP
    padding = Padding.all(16.dp),                          // literal
    gap = Dp.token(TokenRefs.space.md),                    // ref
    border = Border.all(width = 1.dp,
                        color = Color.token(TokenRefs.colors.borderSubtle)), // refs nest into Border
)
```

The IR plugin extracts top-level `val FooStyle = Style(...)` declarations into hashed atomic CSS classes. Inline `Style(...)` calls inside `@Composable` bodies still extract — Compose Compiler's lowering is followed through temp-locals during evaluation.

## Project layout

```
unicompose-style/         # Style / Color / Dp / Sp / Border / Shadow data classes
unicompose/               # mechanism layer: expect/actual UA-equivalent primitives
unicompose-base/          # opinionated themed widgets + Tokens + UnicomposeTheme
unicompose-css-extractor/ # composite-build Kotlin IR compiler plugin + Gradle plugin
samples/
├── kitchen-sink/         # widget gallery — every primitive on every target
└── todo-app/             # focused app sample — full Phase 2 token-driven styling
tests/
├── visual/               # Playwright DOM + canvas goldens
└── snapshot-android/     # Paparazzi Android goldens
```

## Architecture notes

**Three-layer split.** `unicompose-style` is pure-Kotlin data classes with zero Compose dependency. `unicompose` is the mechanism layer — `expect`/`actual` widgets that lower to Compose UI on Skia targets and to bare `<input>`/`<button>`/`<span>` on DOM. `unicompose-base` layers an opinionated design system on top. Apps pick how much to opt into; replacing `unicompose-base` with your own design system (or a Material3-shaped one) is intended.

**Single-module backends per layer.** Kotlin's `expect`/`actual` requires producer + consumer in the same Gradle module across source sets, so each layer ships one module with `commonMain` + `composeAppMain` (Android+iOS+wasmJs canvas, sharing Skia-side actuals) + `jsMain` (DOM, with separate actuals using Compose HTML). Mirrors how React Strict DOM ships one package and lets the bundler pick the right file.

**No Material3 dependency.** The mechanism layer used to delegate to `androidx.compose.material3.Checkbox` etc. on the Skia side, which leaked Material's purple `colorScheme.primary` and oversized chrome through to Android/iOS. Now the CMP actuals are foundation primitives (`Box` + `Modifier.toggleable` + `Canvas`-drawn checkmarks) so DOM and CMP match shape.

**Build-time CSS extraction (StyleX-aligned).** The `unicompose-css-extractor` Kotlin compiler plugin walks IR for `Style(...)` constructor calls during `compileKotlinJs`, evaluates literal arguments + token references, and emits hashed atomic CSS classes to `unicompose-generated.css`. Identical hashes to what the runtime AtomicCss path would produce, so the runtime stays as a graceful fallback for non-extractable dynamic styles. Cross-module aggregation works through Gradle "variants and attributes" — each module exposes a consumable configuration tagged with `Category=unicompose-extracted-css`, and consumers automatically pick up CSS from every dep that applies the plugin.

**Cross-platform default mismatches are explicit.** CSS flex defaults to `align-items: stretch`; Compose Row/Column default to `Top`/`Start`. unicompose emits explicit defaults on web so the two backends match when style props are unset. Same logic for `flex-direction` (defaults to `column` like React Native, not `row` like CSS).

**Compose for iOS is verified via the wasmJs canvas Playwright golden.** Both render through Skia, both use the same `composeAppMain` actuals, so the canvas screenshot transitively covers the iOS render path. Native iOS screenshot tests (XCUITest / swift-snapshot-testing) are post-v0.1 — see PLAN.md for why.

## Roadmap

- v0.1 ship-gate items: tagged release + first publish, Xcode project scaffold for the iOS samples
- post-v0.1: more widget primitives (Slider, Tabs, NavHost), font bundling, blurred-shadow custom drawing on CMP, native iOS goldens

See [PLAN.md](PLAN.md) for the long list, scope notes, and post-v0.1 cleanup items (e.g. proper `pluginManagement` publishing instead of composite-build, Gradle variant integration with the KMP attribute schema).
