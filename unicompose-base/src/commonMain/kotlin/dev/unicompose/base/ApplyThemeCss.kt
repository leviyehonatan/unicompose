package dev.unicompose.base

/**
 * Platform-specific theme application hook.
 *
 * On the web: writes the active token set as `--uc-*` CSS custom properties on
 * the document root, so any `var(--uc-...)` reference in the linked
 * `unicompose-generated.css` (or runtime AtomicCss <style>) resolves to the
 * right value. Theme switching becomes "rewrite a handful of properties on
 * <html>" — no recomposition needed for the styled subtree.
 *
 * On CMP (Android/iOS/wasmJs canvas): no-op. Native targets resolve token
 * values directly through the in-memory `Tokens` object via `LocalTokens` /
 * `currentTokens()`; CSS variables don't apply.
 *
 * Called from [UnicomposeTheme] inside a SideEffect so the platform write
 * happens after every composition where the active tokens may have changed.
 */
internal expect fun applyThemeCssVariables(tokens: Tokens)
