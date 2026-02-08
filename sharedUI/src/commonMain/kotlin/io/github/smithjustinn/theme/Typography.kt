package io.github.smithjustinn.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf

// We'll use the default Material3 Typography but we could also define custom styles if needed
// For now, let's just create a provider for consistency

val LocalAppTypography = staticCompositionLocalOf { Typography() }
