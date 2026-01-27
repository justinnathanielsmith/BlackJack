package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.ui.theme.PokerTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.ceil

private data class CardLayoutInfo(
    val position: Offset,
    val size: Size,
)

private data class GridMetrics(
    val cells: GridCells,
    val maxWidth: androidx.compose.ui.unit.Dp,
)

private data class GridSpacing(
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val topPadding: androidx.compose.ui.unit.Dp,
    val bottomPadding: androidx.compose.ui.unit.Dp,
    val verticalSpacing: androidx.compose.ui.unit.Dp,
    val horizontalSpacing: androidx.compose.ui.unit.Dp,
)

private data class GridLayoutConfig(
    val metrics: GridMetrics,
    val spacing: GridSpacing,
)

data class GridCardState(
    val cards: ImmutableList<CardState>,
    val lastMatchedIds: ImmutableList<Int> = persistentListOf(),
    val isPeeking: Boolean = false,
)

data class GridSettings(
    val displaySettings: CardDisplaySettings = CardDisplaySettings(),
    val showComboExplosion: Boolean = false,
)

data class GridScreenConfig(
    val screenWidth: androidx.compose.ui.unit.Dp,
    val screenHeight: androidx.compose.ui.unit.Dp,
    val isWide: Boolean,
    val isLandscape: Boolean,
    val isCompactHeight: Boolean,
)

@Composable
fun GameGrid(
    gridCardState: GridCardState,
    settings: GridSettings,
    onCardClick: (Int) -> Unit,
) {
    val cardLayouts = remember { mutableStateMapOf<Int, CardLayoutInfo>() }
    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .drawBehind {
                    // Felt Background
                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors = listOf(PokerTheme.FeltGreen, PokerTheme.FeltGreenDark),
                                center = center,
                                radius = size.maxDimension / 1.5f,
                            ),
                    )

                    // Wood Border ("Racetrack")
                    drawRect(
                        color = PokerTheme.OakWood,
                        style = Stroke(width = 16.dp.toPx()),
                    )
                }.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                ).onGloballyPositioned { layoutCoordinates ->
                    gridPosition = layoutCoordinates.positionInRoot()
                },
        contentAlignment = Alignment.Center,
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight
        val isCompactHeight = screenHeight < 500.dp
        val isWide = screenWidth > 800.dp

        val screenConfig =
            GridScreenConfig(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isWide = isWide,
                isLandscape = isLandscape,
                isCompactHeight = isCompactHeight,
            )

        val metrics =
            remember(gridCardState.cards.size, screenConfig) {
                calculateGridMetrics(gridCardState.cards.size, screenConfig)
            }

        val spacing = remember(isWide, isCompactHeight) { calculateGridSpacing(isWide, isCompactHeight) }

        val layoutConfig = remember(metrics, spacing) { GridLayoutConfig(metrics, spacing) }

        GridContent(
            gridCardState = gridCardState,
            layoutConfig = layoutConfig,
            settings = settings,
            onCardClick = onCardClick,
            cardLayouts = cardLayouts,
        )

        GridExplosionEffect(
            show = settings.showComboExplosion,
            lastMatchedIds = gridCardState.lastMatchedIds,
            cardLayouts = cardLayouts,
            gridPosition = gridPosition,
        )
    }
}

@Composable
private fun GridContent(
    gridCardState: GridCardState,
    layoutConfig: GridLayoutConfig,
    settings: GridSettings,
    onCardClick: (Int) -> Unit,
    cardLayouts: SnapshotStateMap<Int, CardLayoutInfo>,
) {
    LazyVerticalGrid(
        columns = layoutConfig.metrics.cells,
        contentPadding =
            PaddingValues(
                start = layoutConfig.spacing.horizontalPadding,
                top = layoutConfig.spacing.topPadding,
                end = layoutConfig.spacing.horizontalPadding,
                bottom = layoutConfig.spacing.bottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(layoutConfig.spacing.verticalSpacing),
        horizontalArrangement = Arrangement.spacedBy(layoutConfig.spacing.horizontalSpacing),
        modifier =
            Modifier
                .fillMaxHeight()
                .widthIn(max = layoutConfig.metrics.maxWidth),
    ) {
        items(gridCardState.cards, key = { it.id }) { card ->
            PlayingCard(
                content =
                    CardContent(
                        suit = card.suit,
                        rank = card.rank,
                        visualState =
                            CardVisualState(
                                isFaceUp = card.isFaceUp || gridCardState.isPeeking,
                                isRecentlyMatched = gridCardState.lastMatchedIds.contains(card.id),
                                isError = card.isError,
                            ),
                    ),
                settings = settings.displaySettings,
                onClick = { onCardClick(card.id) },
                modifier =
                    Modifier.onGloballyPositioned { layoutCoordinates ->
                        cardLayouts[card.id] =
                            CardLayoutInfo(
                                position = layoutCoordinates.positionInRoot(),
                                size = layoutCoordinates.size.toSize(),
                            )
                    },
            )
        }
    }
}

@Composable
private fun GridExplosionEffect(
    show: Boolean,
    lastMatchedIds: ImmutableList<Int>,
    cardLayouts: Map<Int, CardLayoutInfo>,
    gridPosition: Offset,
) {
    if (show && lastMatchedIds.isNotEmpty()) {
        val matchInfos = lastMatchedIds.mapNotNull { cardLayouts[it] }
        if (matchInfos.isNotEmpty()) {
            val averageRootPosition =
                matchInfos
                    .fold(Offset.Zero) { acc, info ->
                        acc + info.position + Offset(info.size.width / 2, info.size.height / 2)
                    }.let { it / matchInfos.size.toFloat() }

            val relativeCenter = averageRootPosition - gridPosition

            ExplosionEffect(
                modifier = Modifier.fillMaxSize(),
                particleCount = 60,
                centerOverride = relativeCenter,
            )
        }
    }
}

private fun calculateGridMetrics(
    cardCount: Int,
    config: GridScreenConfig,
): GridMetrics =
    when {
        config.isLandscape && config.isCompactHeight -> {
            calculateCompactLandscapeMetrics(cardCount, config.screenWidth)
        }

        config.isWide -> {
            calculateWideMetrics(cardCount, config.screenWidth, config.screenHeight)
        }

        else -> {
            calculatePortraitMetrics(
                cardCount,
                config.screenWidth,
                config.screenHeight,
                config.isCompactHeight,
                config.isWide,
            )
        }
    }

private fun calculateCompactLandscapeMetrics(
    cardCount: Int,
    screenWidth: androidx.compose.ui.unit.Dp,
): GridMetrics {
    val cols =
        when {
            cardCount <= SMALL_GRID_THRESHOLD -> COMPACT_LANDSCAPE_COLS_SMALL
            cardCount <= MEDIUM_GRID_THRESHOLD -> COMPACT_LANDSCAPE_COLS_MEDIUM
            cardCount <= LARGE_GRID_THRESHOLD -> COMPACT_LANDSCAPE_COLS_LARGE
            else -> COMPACT_LANDSCAPE_COLS_DEFAULT
        }
    return GridMetrics(GridCells.Fixed(cols), screenWidth)
}

private fun calculateWideMetrics(
    cardCount: Int,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
): GridMetrics {
    val hPadding = 64.dp
    val vPadding = 32.dp
    val spacing = 16.dp
    val availableWidth = screenWidth - hPadding
    val availableHeight = screenHeight - vPadding

    var bestCols = MIN_GRID_COLS_WIDE
    var maxCardHeight = 0.dp

    val maxCols = minOf(cardCount, MAX_GRID_COLS_WIDE)
    for (cols in MIN_GRID_COLS_WIDE..maxCols) {
        val rows = ceil(cardCount.toFloat() / cols).toInt()
        val wBasedCardWidth = (availableWidth - (spacing * (cols - 1))) / cols
        val hFromW = wBasedCardWidth / CARD_ASPECT_RATIO
        val hFromH = (availableHeight - (spacing * (rows - 1))) / rows
        val possibleHeight = if (hFromW < hFromH) hFromW else hFromH

        if (possibleHeight > maxCardHeight) {
            maxCardHeight = possibleHeight
            bestCols = cols
        }
    }

    val finalCardWidth = maxCardHeight * CARD_ASPECT_RATIO
    val calculatedWidth = (finalCardWidth * bestCols) + (spacing * (bestCols - 1)) + hPadding
    return GridMetrics(GridCells.Fixed(bestCols), calculatedWidth.coerceAtMost(screenWidth))
}

private fun calculatePortraitMetrics(
    cardCount: Int,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    isCompactHeight: Boolean,
    isWide: Boolean,
): GridMetrics {
    val spacing = if (isCompactHeight) 6.dp else 12.dp
    val hPadding = if (isWide) 32.dp else 16.dp
    val vPadding = if (isCompactHeight) 16.dp else 32.dp

    val availableWidth = screenWidth - (hPadding * 2)
    val availableHeight = screenHeight - vPadding

    val cols =
        when {
            cardCount <= SMALL_GRID_THRESHOLD -> PORTRAIT_COLS_SMALL
            cardCount <= MEDIUM_GRID_THRESHOLD -> PORTRAIT_COLS_MEDIUM
            else -> PORTRAIT_COLS_DEFAULT
        }
    val rows = ceil(cardCount.toFloat() / cols).toInt()

    val maxW = (availableWidth - (spacing * (cols - 1))) / cols
    val maxH = (availableHeight - (spacing * (rows - 1))) / rows

    val wFromH = maxH * CARD_ASPECT_RATIO
    val finalCardWidth = minOf(maxW, wFromH).coerceAtLeast(60.dp)
    val calculatedWidth = (finalCardWidth * cols) + (spacing * (cols - 1)) + (hPadding * 2)

    return GridMetrics(GridCells.Fixed(cols), calculatedWidth.coerceAtMost(screenWidth))
}

private fun calculateGridSpacing(
    isWide: Boolean,
    isCompactHeight: Boolean,
): GridSpacing {
    val hPadding = if (isWide) 32.dp else 16.dp
    val topPadding = if (isCompactHeight) 8.dp else 16.dp
    val bottomPadding = if (isCompactHeight) 8.dp else 16.dp

    val vSpacing =
        when {
            isCompactHeight -> 4.dp
            isWide -> 16.dp
            else -> 12.dp
        }

    val hSpacing =
        when {
            isCompactHeight -> 6.dp
            isWide -> 16.dp
            else -> 12.dp
        }

    return GridSpacing(
        horizontalPadding = hPadding,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        verticalSpacing = vSpacing,
        horizontalSpacing = hSpacing,
    )
}

private const val SMALL_GRID_THRESHOLD = 12
private const val MEDIUM_GRID_THRESHOLD = 20
private const val LARGE_GRID_THRESHOLD = 24

private const val COMPACT_LANDSCAPE_COLS_SMALL = 6
private const val COMPACT_LANDSCAPE_COLS_MEDIUM = 7
private const val COMPACT_LANDSCAPE_COLS_LARGE = 8
private const val COMPACT_LANDSCAPE_COLS_DEFAULT = 10

private const val PORTRAIT_COLS_SMALL = 3
private const val PORTRAIT_COLS_MEDIUM = 4
private const val PORTRAIT_COLS_DEFAULT = 4

private const val MIN_GRID_COLS_WIDE = 4
private const val MAX_GRID_COLS_WIDE = 12

private const val CARD_ASPECT_RATIO = 0.75f
