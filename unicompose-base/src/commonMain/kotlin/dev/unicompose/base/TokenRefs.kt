package dev.unicompose.base

/**
 * Compile-time-resolvable references to design tokens by CSS variable name.
 *
 * Each `const val` here is the CSS variable name the corresponding token slot
 * lowers to on the web. Top-level Style declarations using these refs become
 * statically extractable by the unicompose-css-extractor compiler plugin —
 * the variable name is a known string constant at IR time, while
 * `currentTokens().colors.accent` is a runtime Composable read invisible to
 * the extractor.
 *
 * On the web: `Style(colorRef = TokenRefs.colors.accent)` lowers to
 *   `.uc-{hash} { color: var(--uc-colors-accent); }`
 * Theme switching (light ↔ dark) changes the value of `--uc-colors-accent`
 * at the document root via UnicomposeTheme's CSS variable emission, so the
 * already-applied class re-renders without recomposition.
 *
 * On CMP: refs are resolved through the active Tokens via the runtime
 * resolver path. Same end result, native semantics.
 */
public object TokenRefs {

    public object colors {
        public const val accent: String = "--uc-colors-accent"
        public const val onAccent: String = "--uc-colors-onAccent"
        public const val bgPage: String = "--uc-colors-bgPage"
        public const val bgSurface: String = "--uc-colors-bgSurface"
        public const val bgSubtle: String = "--uc-colors-bgSubtle"
        public const val textPrimary: String = "--uc-colors-textPrimary"
        public const val textSecondary: String = "--uc-colors-textSecondary"
        public const val borderSubtle: String = "--uc-colors-borderSubtle"
        public const val error: String = "--uc-colors-error"
        public const val success: String = "--uc-colors-success"
    }

    public object space {
        public const val xs: String = "--uc-space-xs"
        public const val sm: String = "--uc-space-sm"
        public const val md: String = "--uc-space-md"
        public const val lg: String = "--uc-space-lg"
        public const val xl: String = "--uc-space-xl"
    }

    public object type {
        public const val xs: String = "--uc-type-xs"
        public const val sm: String = "--uc-type-sm"
        public const val md: String = "--uc-type-md"
        public const val lg: String = "--uc-type-lg"
        public const val xl: String = "--uc-type-xl"
    }

    public object radii {
        public const val sm: String = "--uc-radii-sm"
        public const val md: String = "--uc-radii-md"
        public const val lg: String = "--uc-radii-lg"
    }
}
