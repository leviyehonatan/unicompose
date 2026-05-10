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
