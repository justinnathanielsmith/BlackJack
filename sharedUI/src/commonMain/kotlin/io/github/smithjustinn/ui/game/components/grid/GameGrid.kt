package io.github.smithjustinn.ui.game.components.grid

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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.ui.assets.getPreferredColor
import io.github.smithjustinn.ui.assets.toColor
import io.github.smithjustinn.ui.game.components.cards.CardContent
import io.github.smithjustinn.ui.game.components.cards.CardVisualState
import io.github.smithjustinn.ui.game.components.cards.PlayingCard
import io.github.smithjustinn.ui.game.components.effects.ExplosionEffect
import io.github.smithjustinn.ui.game.components.effects.ScoreFlyingEffect
import kotlinx.collections.immutable.ImmutableList

private data class CardLayoutInfo(
    val position: Offset,
    val size: Size,
)

@Composable
internal fun GameGrid(
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
            theme = settings.cardTheme.back,
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    ).onGloballyPositioned { layoutCoordinates ->
                        gridPosition = layoutCoordinates.positionInRoot()
                    },
        )
        val layoutConfig =
            rememberGridLayoutConfig(
                screenWidth = maxWidth,
                screenHeight = maxHeight,
                cardCount = gridCardState.cards.size,
            )

        GridContent(
            gridCardState = gridCardState,
            layoutConfig = layoutConfig,
            screenHeight = maxHeight,
            gridPosition = gridPosition,
            settings = settings,
            onCardClick = onCardClick,
            cardLayouts = cardLayouts,
        )

        GridEffects(
            gridCardState = gridCardState,
            settings = settings,
            cardLayouts = cardLayouts,
            gridPosition = gridPosition,
            scorePositionInRoot = scorePositionInRoot,
        )
    }
}

@Composable
private fun rememberGridLayoutConfig(
    screenWidth: Dp,
    screenHeight: Dp,
    cardCount: Int,
): GridLayoutConfig {
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

    val metrics = remember(cardCount, screenConfig) { calculateGridMetrics(cardCount, screenConfig) }
    val spacing = remember(isWide, isCompactHeight) { calculateGridSpacing(isWide, isCompactHeight) }

    return remember(metrics, spacing) { GridLayoutConfig(metrics, spacing) }
}

@Composable
private fun GridEffects(
    gridCardState: GridCardState,
    settings: GridSettings,
    cardLayouts: Map<Int, CardLayoutInfo>,
    gridPosition: Offset,
    scorePositionInRoot: Offset,
) {
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

private const val COMPACT_HEIGHT_THRESHOLD_DP = 500
private const val WIDE_WIDTH_THRESHOLD_DP = 800

private const val FAN_ANGLE_MAX = 30f // Total fan spread angle
private const val FAN_SPREAD_MAX = 120f // Horizontal spread distance
private const val MUCK_BOTTOM_OFFSET_DP = 20
private const val MUCK_TARGET_FALLBACK_Y = 1000

private const val CENTER_OFFSET_FRACTION = 0.5f

@Composable
private fun ScoreFlyingGridEffect(
    lastMatchedIds: ImmutableList<Int>,
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

@Composable
private fun GridContent(
    gridCardState: GridCardState,
    layoutConfig: GridLayoutConfig,
    screenHeight: Dp,
    gridPosition: Offset,
    settings: GridSettings,
    onCardClick: (Int) -> Unit,
    cardLayouts: SnapshotStateMap<Int, CardLayoutInfo>,
) {
    val lastMatchedIdsSet =
        remember(gridCardState.lastMatchedIds) {
            gridCardState.lastMatchedIds.toSet()
        }

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
            // Bolt: Pass boolean to prevent unnecessary recompositions of GridItem when matched list changes
            GridItem(
                index = index,
                totalCards = gridCardState.cards.size,
                card = card,
                isPeeking = gridCardState.isPeeking,
                isRecentlyMatched = lastMatchedIdsSet.contains(card.id),
                cardTheme = settings.cardTheme,
                areSuitsMultiColored = settings.areSuitsMultiColored,
                isThirdEyeEnabled = settings.isThirdEyeEnabled,
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
    isRecentlyMatched: Boolean,
    cardTheme: CardTheme,
    areSuitsMultiColored: Boolean,
    isThirdEyeEnabled: Boolean,
    maxWidth: Dp,
    screenHeight: Dp,
    gridPosition: Offset,
    cardLayouts: SnapshotStateMap<Int, CardLayoutInfo>,
    onCardClick: (Int) -> Unit,
) {
    val density = LocalDensity.current
    // Bolt: Defer reading layout info until needed (when matched) to avoid
    // double-composition during initial layout (position write -> invalidation -> read).
    val layoutInfo = if (card.isMatched) cardLayouts[card.id] else null

    // Bolt: Memoize expensive layout calculations to prevent redundant work on every timer update
    val cardMuckTarget =
        rememberCardMuckTarget(
            index = index,
            totalCards = totalCards,
            density = density,
            maxWidth = maxWidth,
            screenHeight = screenHeight,
            gridPosition = gridPosition,
            layoutInfo = layoutInfo,
        )

    val onClick = remember(card.id, onCardClick) { { onCardClick(card.id) } }
    val modifier =
        remember(card.id, cardLayouts) {
            Modifier.onGloballyPositioned { layoutCoordinates ->
                cardLayouts[card.id] =
                    CardLayoutInfo(
                        position = layoutCoordinates.positionInRoot(),
                        size = layoutCoordinates.size.toSize(),
                    )
            }
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
                        isRecentlyMatched = isRecentlyMatched,
                        isError = card.isError,
                    ),
            ),
        theme = cardTheme,
        backColor = cardTheme.backColorHex?.toColor() ?: cardTheme.back.getPreferredColor(),
        areSuitsMultiColored = areSuitsMultiColored,
        wasSeen = card.wasSeen,
        isThirdEyeEnabled = isThirdEyeEnabled,
        muckTargetOffset = cardMuckTarget.offset,
        muckTargetRotation = cardMuckTarget.rotation,
        onClick = onClick,
        modifier = modifier,
    )
}

private data class CardMuckTarget(
    val offset: IntOffset,
    val rotation: Float,
)

@Composable
private fun rememberCardMuckTarget(
    index: Int,
    totalCards: Int,
    density: Density,
    maxWidth: Dp,
    screenHeight: Dp,
    gridPosition: Offset,
    layoutInfo: CardLayoutInfo?,
): CardMuckTarget {
    val (fanRotation, fanSpreadX) =
        remember(index, totalCards) {
            val relativeIndex = index.toFloat() / (totalCards - 1).coerceAtLeast(1) - CENTER_OFFSET_FRACTION
            val rotation = relativeIndex * FAN_ANGLE_MAX
            val spreadX = relativeIndex * FAN_SPREAD_MAX
            rotation to spreadX
        }

    val offset =
        remember(
            density,
            maxWidth,
            screenHeight,
            gridPosition,
            layoutInfo,
            fanSpreadX,
        ) {
            with(density) {
                val muckTarget =
                    Offset(
                        maxWidth.toPx() / 2 + fanSpreadX.dp.toPx(),
                        screenHeight.toPx() - MUCK_BOTTOM_OFFSET_DP.dp.toPx(),
                    )
                layoutInfo?.let { info ->
                    IntOffset(
                        (muckTarget.x - (info.position.x - gridPosition.x) - info.size.width / 2).toInt(),
                        (muckTarget.y - (info.position.y - gridPosition.y) - info.size.height / 2).toInt(),
                    )
                } ?: IntOffset(0, MUCK_TARGET_FALLBACK_Y)
            }
        }

    return remember(offset, fanRotation) {
        CardMuckTarget(offset, fanRotation)
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

            key(lastMatchedIds) {
                ExplosionEffect(
                    modifier = Modifier.fillMaxSize(),
                    particleCount = 60,
                    centerOverride = relativeCenter,
                )
            }
        }
    }
}
