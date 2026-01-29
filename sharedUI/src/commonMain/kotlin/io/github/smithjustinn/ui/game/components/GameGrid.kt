package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.theme.PokerTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.ceil
import kotlin.random.Random

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

@Suppress("LongMethod", "VariableNaming", "MagicNumber", "CyclomaticComplexMethod")
@Composable
fun GameGrid(
    gridCardState: GridCardState,
    settings: GridSettings,
    onCardClick: (Int) -> Unit,
    scorePositionInRoot: Offset = Offset.Zero,
) {
    val cardLayouts = remember { mutableStateMapOf<Int, CardLayoutInfo>() }
    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        GridBackground(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    ).onGloballyPositioned { layoutCoordinates ->
                        gridPosition = layoutCoordinates.positionInRoot()
                    },
        )
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight
        val isCompactHeight = screenHeight < COMPACT_HEIGHT_THRESHOLD_DP.dp
        val isWide = screenWidth > WIDE_WIDTH_THRESHOLD_DP.dp

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
            screenHeight = screenHeight,
            gridPosition = gridPosition,
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

        ScoreFlyingGridEffect(
            lastMatchedIds = gridCardState.lastMatchedIds,
            cardLayouts = cardLayouts,
            gridPosition = gridPosition,
            scorePositionInRoot = scorePositionInRoot,
        )
    }
}

@Composable
private fun ScoreFlyingGridEffect(
    lastMatchedIds: kotlinx.collections.immutable.ImmutableList<Int>,
    cardLayouts: Map<Int, CardLayoutInfo>,
    gridPosition: Offset,
    scorePositionInRoot: Offset,
) {
    if (lastMatchedIds.isNotEmpty() && scorePositionInRoot != Offset.Zero) {
        val matchPositions =
            lastMatchedIds.mapNotNull { id ->
                cardLayouts[id]?.let { info ->
                    info.position - gridPosition + Offset(info.size.width / 2, info.size.height / 2)
                }
            }

        if (matchPositions.isNotEmpty()) {
            val relativeTarget = scorePositionInRoot - gridPosition
            ScoreFlyingEffect(
                matchPositions = matchPositions,
                targetPosition = relativeTarget,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private const val FAN_ANGLE_MAX = 30f // Total fan spread angle
private const val FAN_SPREAD_MAX = 120f // Horizontal spread distance
private const val MUCK_BOTTOM_OFFSET_DP = 20
private const val MUCK_TARGET_FALLBACK_Y = 1000

@Composable
private fun GridContent(
    gridCardState: GridCardState,
    layoutConfig: GridLayoutConfig,
    screenHeight: androidx.compose.ui.unit.Dp,
    gridPosition: Offset,
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
        itemsIndexed(gridCardState.cards, key = { _, card -> card.id }) { index, card ->
            GridItem(
                index = index,
                totalCards = gridCardState.cards.size,
                card = card,
                isPeeking = gridCardState.isPeeking,
                lastMatchedIds = gridCardState.lastMatchedIds,
                displaySettings = settings.displaySettings,
                maxWidth = layoutConfig.metrics.maxWidth,
                screenHeight = screenHeight,
                gridPosition = gridPosition,
                cardLayouts = cardLayouts,
                onCardClick = onCardClick,
            )
        }
    }
}

@Composable
private fun GridItem(
    index: Int,
    totalCards: Int,
    card: CardState,
    isPeeking: Boolean,
    lastMatchedIds: kotlinx.collections.immutable.ImmutableList<Int>,
    displaySettings: CardDisplaySettings,
    maxWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    gridPosition: Offset,
    cardLayouts: SnapshotStateMap<Int, CardLayoutInfo>,
    onCardClick: (Int) -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val layoutInfo = cardLayouts[card.id]

    // Calculate fan-out effect (player's hand)
    val relativeIndex = (index.toFloat() / (totalCards - 1).coerceAtLeast(1)) - CENTER_OFFSET_FRACTION // -0.5 to 0.5
    val fanRotation = relativeIndex * FAN_ANGLE_MAX
    val fanSpreadX = relativeIndex * FAN_SPREAD_MAX

    val muckTargetOffset =
        with(density) {
            val muckTarget =
                Offset(
                    (maxWidth.toPx() / 2) + fanSpreadX.dp.toPx(),
                    screenHeight.toPx() - MUCK_BOTTOM_OFFSET_DP.dp.toPx(),
                )
            layoutInfo?.let { info ->
                IntOffset(
                    (muckTarget.x - (info.position.x - gridPosition.x) - info.size.width / 2).toInt(),
                    (muckTarget.y - (info.position.y - gridPosition.y) - info.size.height / 2).toInt(),
                )
            } ?: IntOffset(0, MUCK_TARGET_FALLBACK_Y)
        }

    PlayingCard(
        content =
            CardContent(
                suit = card.suit,
                rank = card.rank,
                visualState =
                    CardVisualState(
                        isFaceUp = card.isFaceUp || isPeeking,
                        isMatched = card.isMatched,
                        isRecentlyMatched = lastMatchedIds.contains(card.id),
                        isError = card.isError,
                    ),
            ),
        settings = displaySettings,
        muckTargetOffset = muckTargetOffset,
        muckTargetRotation = fanRotation,
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

private const val COMPACT_HEIGHT_THRESHOLD_DP = 500
private const val WIDE_WIDTH_THRESHOLD_DP = 800

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
private const val CENTER_OFFSET_FRACTION = 0.5f
private const val ROTATION_180_DEGREES = 180f
private const val FELT_RADIAL_CENTER_FRACTION = 0.0f
private const val FELT_RADIAL_CENTER_FOCUS_FRACTION = 0.8f
private const val FELT_RADIAL_RADIUS_MULTIPLIER = 1.2f
private const val FELT_TEXTURE_SEED = 42
private const val FELT_TEXTURE_POINT_COUNT = 1000
private const val FELT_TEXTURE_ALPHA = 0.15f
private const val WOOD_BORDER_ALPHA = 0.4f
private const val WOOD_HIGHLIGHT_ALPHA = 0.2f

@Composable
private fun GridBackground(modifier: Modifier = Modifier) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    Box(
        modifier =
            modifier.drawBehind {
                // 1. Intense Felt with "Spotlight" lighting
                drawRect(
                    brush =
                        Brush.radialGradient(
                            FELT_RADIAL_CENTER_FRACTION to colors.feltGreen,
                            FELT_RADIAL_CENTER_FOCUS_FRACTION to colors.feltGreen, // Sustained center color
                            1.0f to colors.feltGreenDark, // Sharper falloff
                            center = center.copy(y = 0f),
                            radius = size.maxDimension * FELT_RADIAL_RADIUS_MULTIPLIER,
                        ),
                )

                // 2. Tactile Felt Texture using drawPoints for better visibility
                val random = Random(FELT_TEXTURE_SEED)
                val points =
                    List(FELT_TEXTURE_POINT_COUNT) {
                        Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                    }
                drawPoints(
                    points = points,
                    pointMode = PointMode.Points,
                    color = Color.Black.copy(alpha = FELT_TEXTURE_ALPHA),
                    strokeWidth = 2f,
                )

                // 3. Wood Border with Pronounced Bevel Effect
                val strokeWidth = spacing.medium.toPx()
                val bevelWidth = spacing.extraSmall.toPx()

                // Outer Shadow (Ambient Occlusion)
                drawRect(
                    color = Color.Black.copy(alpha = WOOD_BORDER_ALPHA),
                    style = Stroke(width = strokeWidth + bevelWidth),
                )

                // The Wood ("Racetrack")
                drawRect(
                    color = colors.oakWood,
                    style = Stroke(width = strokeWidth),
                )

                // Inner Highlight (Direct Reflection)
                drawRect(
                    color = Color.White.copy(alpha = WOOD_HIGHLIGHT_ALPHA),
                    style = Stroke(width = 2.dp.toPx()),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size =
                        size.copy(
                            width = (size.width - strokeWidth).coerceAtLeast(0f),
                            height = (size.height - strokeWidth).coerceAtLeast(0f),
                        ),
                )
            },
    )
}
