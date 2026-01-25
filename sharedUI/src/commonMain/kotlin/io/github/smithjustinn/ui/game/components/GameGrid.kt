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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.CardState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.ceil

private data class CardLayoutInfo(val position: Offset, val size: Size)

private data class GridMetrics(val cells: GridCells, val maxWidth: androidx.compose.ui.unit.Dp)

private data class GridSpacing(
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val topPadding: androidx.compose.ui.unit.Dp,
    val bottomPadding: androidx.compose.ui.unit.Dp,
    val verticalSpacing: androidx.compose.ui.unit.Dp,
    val horizontalSpacing: androidx.compose.ui.unit.Dp,
)

@Composable
fun GameGrid(
    cards: ImmutableList<CardState>,
    onCardClick: (Int) -> Unit,
    isPeeking: Boolean = false,
    lastMatchedIds: ImmutableList<Int> = persistentListOf(),
    showComboExplosion: Boolean = false,
    settings: CardDisplaySettings = CardDisplaySettings(),
) {
    val cardLayouts = remember { mutableStateMapOf<Int, CardLayoutInfo>() }
    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier =
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(
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

        val metrics =
            remember(cards.size, screenWidth, screenHeight, isWide, isLandscape, isCompactHeight) {
                calculateGridMetrics(cards.size, screenWidth, screenHeight, isWide, isLandscape, isCompactHeight)
            }

        val spacing = remember(isWide, isCompactHeight) { calculateGridSpacing(isWide, isCompactHeight) }

        GridContent(
            cards = cards,
            metrics = metrics,
            spacing = spacing,
            isPeeking = isPeeking,
            lastMatchedIds = lastMatchedIds,
            settings = settings,
            onCardClick = onCardClick,
            cardLayouts = cardLayouts,
        )

        GridExplosionEffect(
            show = showComboExplosion,
            lastMatchedIds = lastMatchedIds,
            cardLayouts = cardLayouts,
            gridPosition = gridPosition,
        )
    }
}

@Composable
private fun GridContent(
    cards: ImmutableList<CardState>,
    metrics: GridMetrics,
    spacing: GridSpacing,
    isPeeking: Boolean,
    lastMatchedIds: ImmutableList<Int>,
    settings: CardDisplaySettings,
    onCardClick: (Int) -> Unit,
    cardLayouts: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, CardLayoutInfo>,
) {
    LazyVerticalGrid(
        columns = metrics.cells,
        contentPadding =
        PaddingValues(
            start = spacing.horizontalPadding,
            top = spacing.topPadding,
            end = spacing.horizontalPadding,
            bottom = spacing.bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.verticalSpacing),
        horizontalArrangement = Arrangement.spacedBy(spacing.horizontalSpacing),
        modifier =
        Modifier
            .fillMaxHeight()
            .widthIn(max = metrics.maxWidth),
    ) {
        items(cards, key = { it.id }) { card ->
            PlayingCard(
                suit = card.suit,
                rank = card.rank,
                isFaceUp = card.isFaceUp || isPeeking,
                isRecentlyMatched = lastMatchedIds.contains(card.id),
                isError = card.isError,
                settings = settings,
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
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    isWide: Boolean,
    isLandscape: Boolean,
    isCompactHeight: Boolean,
): GridMetrics = when {
    isLandscape && isCompactHeight -> calculateCompactLandscapeMetrics(cardCount, screenWidth)
    isWide -> calculateWideMetrics(cardCount, screenWidth, screenHeight)
    else -> calculatePortraitMetrics(cardCount, screenWidth, screenHeight, isCompactHeight, isWide)
}

private fun calculateCompactLandscapeMetrics(cardCount: Int, screenWidth: androidx.compose.ui.unit.Dp): GridMetrics {
    val cols =
        when {
            cardCount <= 12 -> 6
            cardCount <= 20 -> 7
            cardCount <= 24 -> 8
            else -> 10
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

    var bestCols = 4
    var maxCardHeight = 0.dp

    val maxCols = minOf(cardCount, 12)
    for (cols in 4..maxCols) {
        val rows = ceil(cardCount.toFloat() / cols).toInt()
        val wBasedCardWidth = (availableWidth - (spacing * (cols - 1))) / cols
        val hFromW = wBasedCardWidth / 0.75f
        val hFromH = (availableHeight - (spacing * (rows - 1))) / rows
        val possibleHeight = if (hFromW < hFromH) hFromW else hFromH

        if (possibleHeight > maxCardHeight) {
            maxCardHeight = possibleHeight
            bestCols = cols
        }
    }

    val finalCardWidth = maxCardHeight * 0.75f
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
            cardCount <= 12 -> 3
            cardCount <= 20 -> 4
            else -> 4
        }
    val rows = ceil(cardCount.toFloat() / cols).toInt()

    val maxW = (availableWidth - (spacing * (cols - 1))) / cols
    val maxH = (availableHeight - (spacing * (rows - 1))) / rows

    val wFromH = maxH * 0.75f
    val finalCardWidth = minOf(maxW, wFromH).coerceAtLeast(60.dp)
    val calculatedWidth = (finalCardWidth * cols) + (spacing * (cols - 1)) + (hPadding * 2)

    return GridMetrics(GridCells.Fixed(cols), calculatedWidth.coerceAtMost(screenWidth))
}

private fun calculateGridSpacing(isWide: Boolean, isCompactHeight: Boolean): GridSpacing {
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
