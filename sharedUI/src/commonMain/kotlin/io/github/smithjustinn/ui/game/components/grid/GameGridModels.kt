package io.github.smithjustinn.ui.game.components.grid

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Dp
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.CardTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class GridMetrics(
    val cells: GridCells,
    val maxWidth: Dp,
)

internal data class GridSpacing(
    val horizontalPadding: Dp,
    val topPadding: Dp,
    val bottomPadding: Dp,
    val verticalSpacing: Dp,
    val horizontalSpacing: Dp,
)

internal data class GridLayoutConfig(
    val metrics: GridMetrics,
    val spacing: GridSpacing,
)

internal data class GridCardState(
    val cards: ImmutableList<CardState>,
    val lastMatchedIds: ImmutableList<Int> = persistentListOf(),
    val isPeeking: Boolean = false,
)

internal data class GridSettings(
    val cardTheme: CardTheme = CardTheme(),
    val areSuitsMultiColored: Boolean = false,
    val isThirdEyeEnabled: Boolean = false,
    val showComboExplosion: Boolean = false,
)

internal data class GridScreenConfig(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val isWide: Boolean,
    val isLandscape: Boolean,
    val isCompactHeight: Boolean,
)
