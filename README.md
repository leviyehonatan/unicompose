# unicompose

Shared UI primitives for Kotlin: write Composables once, render them on Android, iOS, and **real HTML** (not canvas).

`unicompose` is a small, deliberately-constrained widget set that compiles to two backends:

- **Compose Multiplatform** for Android and iOS (Skia-rendered, native-feeling).
- **Compose HTML** for the web — emits real DOM elements with semantic HTML, working SEO, and screen-reader accessibility.

Inspired by [React Strict DOM](https://facebook.github.io/react-strict-dom/). Apache 2.0.

> Status: pre-alpha. v0.1 in progress. See `/Users/nickst/.claude/plans/lets-write-a-plan-rosy-sun.md`.

## Quick example

```kotlin
@Composable
fun Greeting(name: String) {
    UiText("Hello, $name", style = Style(fontSize = 18.sp, color = Color.Black))
}
```

The same function renders as `Text` in Jetpack Compose on Android/iOS and as `<span>` in real DOM on the web.

## Project layout

```
unicompose/         # the library: commonMain expects, per-target actuals
unicompose-style/   # Style data class, layout primitives (pure Kotlin)
samples/
└── kitchen-sink/   # one screen on Android, iOS, and web
```

## Architectural notes

**Single-module backends.** The plan originally split the runtime into separate modules per backend. Kotlin's `expect`/`actual` requires both sides in the same Gradle module across different source sets, so the runtimes are unified into one module with `androidMain`/`iosMain`/`jsMain` source sets — matching how React Strict DOM ships a single package and lets the bundler pick the right file.

**Runtime atomic CSS, not KSP.** On the web target, each unique `Style` is hashed into a class name and registered into a singleton `<style>` element. Identical DOM output, identical SEO/accessibility properties, dramatically less build-time complexity than the originally-planned KSP processor. KSP extraction would only matter for cold-start-critical apps with thousands of unique styles, or for SSR — neither is in scope.

**Cross-platform default mismatches are explicit.** CSS flex defaults to `align-items: stretch`; Compose Row/Column default to `Top`/`Start`. unicompose emits explicit defaults on web so the two backends match when style props are unset. Same logic for `flex-direction` (we default to `column` like React Native, not `row` like CSS).
