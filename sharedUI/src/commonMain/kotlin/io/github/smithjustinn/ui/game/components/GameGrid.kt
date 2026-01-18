package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.smithjustinn.domain.models.CardState
import kotlin.math.ceil

private data class CardLayoutInfo(val position: Offset, val size: Size)

@Composable
fun GameGrid(
    cards: List<CardState>,
    onCardClick: (Int) -> Unit,
    isPeeking: Boolean = false,
    lastMatchedIds: List<Int> = emptyList(),
    showComboExplosion: Boolean = false
) {
    val cardLayouts = remember { mutableStateMapOf<Int, CardLayoutInfo>() }
    var gridPosition by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .onGloballyPositioned { layoutCoordinates ->
                gridPosition = layoutCoordinates.positionInRoot()
            },
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight
        val isCompactHeight = screenHeight < 500.dp
        val isWide = screenWidth > 800.dp
        
        val (gridCells, maxGridWidth) = remember(cards.size, screenWidth, screenHeight, isWide, isLandscape) {
            when {
                isLandscape && isCompactHeight -> {
                    // Landscape phone: optimize for horizontal space
                    val cols = when {
                        cards.size <= 12 -> 6
                        cards.size <= 20 -> 7
                        cards.size <= 24 -> 8
                        else -> 10
                    }
                    GridCells.Fixed(cols) to screenWidth
                }
                isWide -> {
                    // Large screen: At least 4 columns, maximize card height while fitting screen
                    val hPadding = 64.dp 
                    val vPadding = 32.dp // Margin to prevent edge-to-edge
                    val spacing = 16.dp
                    val availableWidth = screenWidth - hPadding
                    val availableHeight = screenHeight - vPadding
                    
                    var bestCols = 4
                    var maxCardHeight = 0.dp
                    
                    // Iterate from 4 columns up to half the cards or max 12
                    val maxCols = minOf(cards.size, 12)
                    for (cols in 4..maxCols) {
                        val rows = ceil(cards.size.toFloat() / cols).toInt()
                        
                        // Card width if limited by available width
                        val wBasedCardWidth = (availableWidth - (spacing * (cols - 1))) / cols
                        // Equivalent height (3:4 ratio)
                        val hFromW = wBasedCardWidth / 0.75f
                        
                        // Card height if limited by available height
                        val hFromH = (availableHeight - (spacing * (rows - 1))) / rows
                        
                        // The maximum height a card can have with this many columns
                        val possibleHeight = if (hFromW < hFromH) hFromW else hFromH
                        
                        if (possibleHeight > maxCardHeight) {
                            maxCardHeight = possibleHeight
                            bestCols = cols
                        }
                    }
                    
                    val finalCardWidth = maxCardHeight * 0.75f
                    val calculatedWidth = (finalCardWidth * bestCols) + (spacing * (bestCols - 1)) + hPadding
                    GridCells.Fixed(bestCols) to calculatedWidth.coerceAtMost(screenWidth)
                }
                else -> {
                    // Mobile Portrait: Adaptive
                    val minSize = when {
                        cards.size <= 12 -> 100.dp
                        cards.size <= 24 -> 80.dp
                        else -> 70.dp
                    }
                    GridCells.Adaptive(minSize = minSize) to screenWidth
                }
            }
        }

        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val horizontalPadding = if (isWide) 32.dp else 16.dp
        val topPadding = if (isCompactHeight) 8.dp else 16.dp

        LazyVerticalGrid(
            columns = gridCells,
            contentPadding = PaddingValues(
                start = horizontalPadding,
                top = topPadding,
                end = horizontalPadding,
                bottom = bottomPadding + (if (isCompactHeight) 8.dp else 16.dp)
            ),
            verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 4.dp else if (isWide) 16.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else if (isWide) 16.dp else 12.dp),
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = maxGridWidth)
        ) {
            items(cards, key = { it.id }) { card ->
                PlayingCard(
                    suit = card.suit,
                    rank = card.rank,
                    isFaceUp = card.isFaceUp || isPeeking,
                    isMatched = card.isMatched,
                    isRecentlyMatched = lastMatchedIds.contains(card.id),
                    isError = card.isError,
                    onClick = { onCardClick(card.id) },
                    modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                        cardLayouts[card.id] = CardLayoutInfo(
                            position = layoutCoordinates.positionInRoot(),
                            size = layoutCoordinates.size.toSize()
                        )
                    }
                )
            }
        }

        if (showComboExplosion && lastMatchedIds.isNotEmpty()) {
            val matchInfos = lastMatchedIds.mapNotNull { cardLayouts[it] }
            if (matchInfos.isNotEmpty()) {
                val averageRootPosition = matchInfos
                    .fold(Offset.Zero) { acc, info -> 
                    acc + info.position + Offset(info.size.width / 2, info.size.height / 2)
                }
                    .let { it / matchInfos.size.toFloat() }
                
                val relativeCenter = averageRootPosition - gridPosition

                ExplosionEffect(
                    modifier = Modifier.fillMaxSize(),
                    particleCount = 60,
                    centerOverride = relativeCenter
                )
            }
        }
    }
}
