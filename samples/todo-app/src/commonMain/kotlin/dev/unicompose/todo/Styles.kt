package dev.unicompose.todo

import dev.unicompose.style.Color
import dev.unicompose.style.FontWeight
import dev.unicompose.style.Padding
import dev.unicompose.style.Style
import dev.unicompose.style.dp
import dev.unicompose.style.sp

// Smoke-test top-level Style declarations.
// The IR plugin should be able to extract literal values from these because
// Compose Compiler only lowers @Composable function BODIES, not top-level
// initializers. If the IR for these initializers is intact we have the
// foundation for a StyleX-style extraction surface.
public val SmokeTestLiteral: Style = Style(
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    color = Color.White,
    padding = Padding.all(16.dp),
)

public val SmokeTestSimple: Style = Style(
    flex = 1f,
)

// Phase 2 smoke test: Style fields can now reference theme tokens by CSS
// variable name. The IR plugin should extract this top-level val and the
// generated CSS should contain `var(...)` for each ref instead of a literal.
// At runtime, the same hashed class is in the linked sheet; theme switches
// just change the value of --uc-* properties at the document root.
public val SmokeTestTokens: Style = Style(
    colorRef = "--uc-colors-textPrimary",
    backgroundColorRef = "--uc-colors-bgSurface",
)

// Same idea but using the TokenRefs constants (compile-time resolvable
// references to the same names). Should produce IDENTICAL output.
public val SmokeTestTokenRefs: Style = Style(
    colorRef = dev.unicompose.base.TokenRefs.colors.textPrimary,
    backgroundColorRef = dev.unicompose.base.TokenRefs.colors.bgSurface,
    gapRef = dev.unicompose.base.TokenRefs.space.md,
)
