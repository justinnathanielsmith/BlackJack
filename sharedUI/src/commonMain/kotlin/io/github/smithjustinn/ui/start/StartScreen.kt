package io.github.smithjustinn.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.start.components.CardPreview
import io.github.smithjustinn.ui.start.components.StartHeader
import io.github.smithjustinn.ui.start.components.DifficultySelectionSection
import io.github.smithjustinn.ui.game.GameScreen
import io.github.smithjustinn.ui.settings.SettingsScreen
import io.github.smithjustinn.ui.stats.StatsScreen
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.settings
import memory_match.sharedui.generated.resources.stats
import org.jetbrains.compose.resources.stringResource

class StartScreen : Screen, JavaSerializable {
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.startScreenModel }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val audioService = graph.audioService

        LaunchedEffect(Unit) {
            screenModel.handleIntent(DifficultyIntent.CheckSavedGame)
        }

        LaunchedEffect(Unit) {
            screenModel.events.collect { event ->
                when (event) {
                    is DifficultyUiEvent.NavigateToGame -> {
                        navigator.push(GameScreen(event.pairs, forceNewGame = event.forceNewGame, mode = event.mode))
                    }
                }
            }
        }

        val textMeasurer = rememberTextMeasurer()
        val suitStyle = TextStyle(
            fontSize = 42.sp,
            color = Color.White.copy(alpha = 0.03f) // Faint white suits on dark blue
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val radius = size.maxDimension
                    // Dark Blue Radial Gradient Background
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A), // Lighter Blue Center
                                Color(0xFF020617)  // Very Dark Blue Outer
                            ),
                            radius = radius,
                            center = center
                        )
                    )
                    
                    val suits = listOf("♥", "♦", "♣", "♠")
                    val patternSize = 120.dp.toPx()
                    val cols = (size.width / patternSize).toInt() + 2
                    val rows = (size.height / patternSize).toInt() + 2
                    
                    for (i in 0 until cols) {
                        for (j in 0 until rows) {
                            val suit = suits[(i + j) % 4]
                            val x = i * patternSize + (if (j % 2 == 0) 0f else patternSize / 2) - patternSize / 2
                            val y = j * patternSize - patternSize / 2
                            
                            rotate(degrees = if ((i + j) % 2 == 0) 15f else -15f, pivot = Offset(x + 20f, y + 20f)) {
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = suit,
                                    topLeft = Offset(x, y),
                                    style = suitStyle
                                )
                            }
                        }
                    }
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val isLandscape = screenWidth > screenHeight
                val isCompactHeight = screenHeight < 500.dp
                val isWide = screenWidth > 800.dp
                val isNarrow = screenWidth < 600.dp
                val useLandscapeLayout = isLandscape && (isCompactHeight || isWide)

                if (useLandscapeLayout) {
                    // LANDSCAPE MODE
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            StartHeader(scale = if (isCompactHeight) 0.75f else 1f)
                            Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 16.dp))
                            CardPreview(
                                modifier = Modifier.height(if (isCompactHeight) 110.dp else 180.dp),
                                cardBackTheme = state.cardBackTheme,
                                cardSymbolTheme = state.cardSymbolTheme
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            DifficultySelectionSection(
                                state = state,
                                onDifficultySelected = { level ->
                                    audioService.playClick()
                                    screenModel.handleIntent(DifficultyIntent.SelectDifficulty(level))
                                },
                                onModeSelected = { mode ->
                                    audioService.playClick()
                                    screenModel.handleIntent(DifficultyIntent.SelectMode(mode))
                                },
                                onStartGame = {
                                    audioService.playClick()
                                    screenModel.handleIntent(
                                        DifficultyIntent.StartGame(
                                            state.selectedDifficulty.pairs,
                                            state.selectedMode
                                        )
                                    )
                                },
                                onResumeGame = {
                                    audioService.playClick()
                                    screenModel.handleIntent(DifficultyIntent.ResumeGame)
                                },
                                modifier = Modifier
                                    .widthIn(max = if (isWide) 650.dp else 450.dp)
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                    
                    // Floating Actions in Landscape (plenty of room)
                    GlobalActionsRow(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(16.dp),
                        onSettingsClick = {
                            audioService.playClick()
                            navigator.push(SettingsScreen())
                        },
                        onStatsClick = {
                            audioService.playClick()
                            navigator.push(StatsScreen())
                        }
                    )
                } else {
                    // PORTRAIT MODE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Move actions into the column to avoid overlap with centered title
                        GlobalActionsRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                            onSettingsClick = {
                                audioService.playClick()
                                navigator.push(SettingsScreen())
                            },
                            onStatsClick = {
                                audioService.playClick()
                                navigator.push(StatsScreen())
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        StartHeader(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            scale = if (isNarrow) 0.75f else 1f
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        CardPreview(
                            modifier = Modifier.height(180.dp),
                            cardBackTheme = state.cardBackTheme,
                            cardSymbolTheme = state.cardSymbolTheme
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        DifficultySelectionSection(
                            state = state,
                            onDifficultySelected = { level ->
                                audioService.playClick()
                                screenModel.handleIntent(DifficultyIntent.SelectDifficulty(level))
                            },
                            onModeSelected = { mode ->
                                audioService.playClick()
                                screenModel.handleIntent(DifficultyIntent.SelectMode(mode))
                            },
                            onStartGame = {
                                audioService.playClick()
                                screenModel.handleIntent(
                                    DifficultyIntent.StartGame(
                                        state.selectedDifficulty.pairs,
                                        state.selectedMode
                                    )
                                )
                            },
                            onResumeGame = {
                                audioService.playClick()
                                screenModel.handleIntent(DifficultyIntent.ResumeGame)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun GlobalActionsRow(
        modifier: Modifier = Modifier,
        onSettingsClick: () -> Unit,
        onStatsClick: () -> Unit
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.End
        ) {
            ActionIconButton(
                icon = AppIcons.Settings,
                contentDescription = stringResource(Res.string.settings),
                onClick = onSettingsClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            ActionIconButton(
                icon = AppIcons.Info,
                contentDescription = stringResource(Res.string.stats),
                onClick = onStatsClick
            )
        }
    }

    @Composable
    private fun ActionIconButton(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        contentDescription: String,
        onClick: () -> Unit
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(Color(0xFF0F1E3D).copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                .size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFF60A5FA), // Light Blue tint
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
