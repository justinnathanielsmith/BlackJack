package io.github.smithjustinn.ui.game.components.grid

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

internal const val SMALL_GRID_THRESHOLD = 12
internal const val MEDIUM_GRID_THRESHOLD = 20
internal const val LARGE_GRID_THRESHOLD = 24

internal const val CARD_ASPECT_RATIO = 0.75f

// Padding and Spacing Constants
private val PADDING_WIDE_H = 64.dp
private val PADDING_WIDE_V = 32.dp
private val SPACING_WIDE = 16.dp
private val PADDING_PORTRAIT_H_WIDE = 32.dp
private val PADDING_PORTRAIT_H_DEFAULT = 16.dp
private val PADDING_PORTRAIT_V_COMPACT = 16.dp
private val PADDING_PORTRAIT_V_DEFAULT = 32.dp
private val SPACING_PORTRAIT_COMPACT = 6.dp
private val SPACING_PORTRAIT_DEFAULT = 12.dp

private val PADDING_GRID_COMPACT = 8.dp
private val PADDING_GRID_DEFAULT = 16.dp
private val SPACING_GRID_V_COMPACT = 4.dp
private val SPACING_GRID_V_WIDE = 16.dp
private val SPACING_GRID_V_DEFAULT = 12.dp
private val SPACING_GRID_H_COMPACT = 6.dp
private val SPACING_GRID_H_WIDE = 16.dp
private val SPACING_GRID_H_DEFAULT = 12.dp

private const val MIN_GRID_COLS_WIDE = 4
private const val MAX_GRID_COLS_WIDE = 12
private const val COMPACT_COLS_SMALL = 6
private const val COMPACT_COLS_MEDIUM = 7
private const val COMPACT_COLS_LARGE = 8
private const val COMPACT_COLS_DEFAULT = 10
private const val MIN_CARD_WIDTH_DP = 60

private const val PORTRAIT_COLS_SMALL = 3
private const val PORTRAIT_COLS_MEDIUM = 4
private const val PORTRAIT_COLS_DEFAULT = 4

internal fun calculateGridMetrics(
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
    screenWidth: Dp,
): GridMetrics {
    val cols =
        when {
            cardCount <= SMALL_GRID_THRESHOLD -> COMPACT_COLS_SMALL
            cardCount <= MEDIUM_GRID_THRESHOLD -> COMPACT_COLS_MEDIUM
            cardCount <= LARGE_GRID_THRESHOLD -> COMPACT_COLS_LARGE
            else -> COMPACT_COLS_DEFAULT
        }
    return GridMetrics(GridCells.Fixed(cols), screenWidth)
}

private fun calculateWideMetrics(
    cardCount: Int,
    screenWidth: Dp,
    screenHeight: Dp,
): GridMetrics {
    val hPadding = PADDING_WIDE_H
    val vPadding = PADDING_WIDE_V
    val spacing = SPACING_WIDE
    val availableWidth = screenWidth - hPadding
    val availableHeight = screenHeight - vPadding

    var bestCols = MIN_GRID_COLS_WIDE
    var maxCardHeight = 0.dp

    val maxCols = minOf(cardCount, MAX_GRID_COLS_WIDE)
    for (cols in MIN_GRID_COLS_WIDE..maxCols) {
        val rows = ceil(cardCount.toFloat() / cols).toInt()
        val wBasedCardWidth = (availableWidth - spacing * (cols - 1)) / cols
        val hFromW = wBasedCardWidth / CARD_ASPECT_RATIO
        val hFromH = (availableHeight - spacing * (rows - 1)) / rows
        val possibleHeight = if (hFromW < hFromH) hFromW else hFromH

        if (possibleHeight > maxCardHeight) {
            maxCardHeight = possibleHeight
            bestCols = cols
        }
    }

    val finalCardWidth = maxCardHeight * CARD_ASPECT_RATIO
    val calculatedWidth = finalCardWidth * bestCols + spacing * (bestCols - 1) + hPadding
    return GridMetrics(GridCells.Fixed(bestCols), calculatedWidth.coerceAtMost(screenWidth))
}

private fun calculatePortraitMetrics(
    cardCount: Int,
    screenWidth: Dp,
    screenHeight: Dp,
    isCompactHeight: Boolean,
    isWide: Boolean,
): GridMetrics {
    val spacing = if (isCompactHeight) SPACING_PORTRAIT_COMPACT else SPACING_PORTRAIT_DEFAULT
    val hPadding = if (isWide) PADDING_PORTRAIT_H_WIDE else PADDING_PORTRAIT_H_DEFAULT
    val vPadding = if (isCompactHeight) PADDING_PORTRAIT_V_COMPACT else PADDING_PORTRAIT_V_DEFAULT

    val availableWidth = screenWidth - hPadding * 2
    val availableHeight = screenHeight - vPadding

    val cols =
        when {
            cardCount <= SMALL_GRID_THRESHOLD -> PORTRAIT_COLS_SMALL
            cardCount <= MEDIUM_GRID_THRESHOLD -> PORTRAIT_COLS_MEDIUM
            else -> PORTRAIT_COLS_DEFAULT
        }
    val rows = ceil(cardCount.toFloat() / cols).toInt()

    val maxW = (availableWidth - spacing * (cols - 1)) / cols
    val maxH = (availableHeight - spacing * (rows - 1)) / rows

    val wFromH = maxH * CARD_ASPECT_RATIO
    val finalCardWidth = minOf(maxW, wFromH).coerceAtLeast(MIN_CARD_WIDTH_DP.dp)
    val calculatedWidth = finalCardWidth * cols + spacing * (cols - 1) + hPadding * 2

    return GridMetrics(GridCells.Fixed(cols), calculatedWidth.coerceAtMost(screenWidth))
}

internal fun calculateGridSpacing(
    isWide: Boolean,
    isCompactHeight: Boolean,
): GridSpacing {
    val hPadding = if (isWide) PADDING_PORTRAIT_H_WIDE else PADDING_PORTRAIT_H_DEFAULT
    val topPadding = if (isCompactHeight) PADDING_GRID_COMPACT else PADDING_GRID_DEFAULT
    val bottomPadding = if (isCompactHeight) PADDING_GRID_COMPACT else PADDING_GRID_DEFAULT

    val vSpacing =
        when {
            isCompactHeight -> SPACING_GRID_V_COMPACT
            isWide -> SPACING_GRID_V_WIDE
            else -> SPACING_GRID_V_DEFAULT
        }

    val hSpacing =
        when {
            isCompactHeight -> SPACING_GRID_H_COMPACT
            isWide -> SPACING_GRID_H_WIDE
            else -> SPACING_GRID_H_DEFAULT
        }

    return GridSpacing(
        horizontalPadding = hPadding,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        verticalSpacing = vSpacing,
        horizontalSpacing = hSpacing,
    )
}
