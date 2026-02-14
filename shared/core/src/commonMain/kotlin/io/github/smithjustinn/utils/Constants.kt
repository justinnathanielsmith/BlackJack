package io.github.smithjustinn.utils

/**
 * Shared constants for the application.
 */
object Constants {
    // Deep Link components
    const val DEEP_LINK_SCHEME = "memorymatch"
    const val DEEP_LINK_HOST_GAME = "game"
    const val DEEP_LINK_PREFIX = "$DEEP_LINK_SCHEME://$DEEP_LINK_HOST_GAME"
    
    const val QUERY_PARAM_MODE = "mode"
    const val QUERY_PARAM_PAIRS = "pairs"
    const val QUERY_PARAM_SEED = "seed"

    // Animation Labels (Compose internal)
    const val LABEL_GLIMMER = "glimmer"
    const val LABEL_AURORA = "aurora"
    const val LABEL_CHIP_ELEVATION = "chipElevation"
    const val LABEL_CHIP_SCALE = "chipScale"
    const val LABEL_CHIP_GLOW_ALPHA = "chipGlowAlpha"
    
    // Feature IDs
    const val FEATURE_FOUR_COLOR_SUITS = "feature_four_color_suits"
}
