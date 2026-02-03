package io.github.smithjustinn.ui.assets

import androidx.compose.ui.graphics.Color
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.ui.game.components.cards.CardBacks

/**
 * Returns the primary color associated with this [CardBackTheme].
 * These colors match the ones used in [CardBacks].
 */
fun CardBackTheme.getPreferredColor(): Color =
    when (this) {
        CardBackTheme.GEOMETRIC -> Color(0xFF1A237E) // Deep Blue (Standard / Geometric)
        CardBackTheme.CLASSIC -> Color(0xFFB71C1C) // Deep Red
        CardBackTheme.PATTERN -> Color(0xFF4527A0) // Deep Purple
        CardBackTheme.POKER -> Color(0xFF004D40) // Deep Teal
    }

/**
 * Converts a hex string to a Compose [Color].
 * Supports formats: "#RRGGBB", "RRGGBB", "#AARRGGBB", "AARRGGBB".
 */
fun String.toColor(): Color {
    val hex = this.removePrefix("#")
    return try {
        when (hex.length) {
            6 -> Color(hex.toLong(16) or 0xFF000000L)
            8 -> Color(hex.toLong(16))
            else -> Color.Gray
        }
    } catch (e: NumberFormatException) {
        Color.Gray
    }
}
