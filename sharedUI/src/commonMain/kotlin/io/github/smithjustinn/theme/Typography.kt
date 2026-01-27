package io.github.smithjustinn.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// We'll use the default Material3 Typography but we could also define custom styles if needed
// For now, let's just create a provider for consistency

val LocalAppTypography = androidx.compose.runtime.staticCompositionLocalOf { Typography() }
