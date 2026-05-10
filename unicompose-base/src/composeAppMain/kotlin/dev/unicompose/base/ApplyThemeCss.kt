package dev.unicompose.base

/** No-op on CMP — native targets resolve tokens via LocalTokens directly. */
internal actual fun applyThemeCssVariables(tokens: Tokens) {
    // Intentionally empty.
}
