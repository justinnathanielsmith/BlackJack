package io.github.smithjustinn.ui.difficulty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.difficulty.components.CardPreview
import io.github.smithjustinn.ui.difficulty.components.DifficultyHeader
import io.github.smithjustinn.ui.difficulty.components.DifficultySelectionSection
import io.github.smithjustinn.ui.game.GameScreen
import io.github.smithjustinn.ui.settings.SettingsScreen
import io.github.smithjustinn.ui.stats.StatsScreen
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.settings
import memory_match.sharedui.generated.resources.stats
import org.jetbrains.compose.resources.stringResource

class DifficultyScreen : Screen, JavaSerializable {
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.difficultyScreenModel }
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

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val isLandscape = screenWidth > screenHeight
            val isCompactHeight = screenHeight < 500.dp
            val isWide = screenWidth > 800.dp
            val isNarrow = screenWidth < 600.dp
            val useLandscapeLayout = isLandscape && (isCompactHeight || isWide)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                if (useLandscapeLayout) {
                    // LANDSCAPE MODE: Side-by-side as preferred by user
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Header + Card Preview
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            DifficultyHeader(scale = if (isCompactHeight) 0.75f else 1f)
                            Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 16.dp))
                            // Reduced height in compact to ensure visibility
                            CardPreview(modifier = Modifier.height(if (isCompactHeight) 110.dp else 180.dp))
                        }

                        // Right Column: Selection Section
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
                                modifier = Modifier.widthIn(max = 450.dp).padding(vertical = 16.dp),
                                compact = true
                            )
                        }
                    }
                } else {
                    // PORTRAIT MODE: Explicit spacer to avoid floating button overlap
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Ensuring enough space for the floating action buttons + status bar
                        // Increased height significantly to prevent overlap with floating icons
                        Spacer(modifier = Modifier.height(132.dp).statusBarsPadding())

                        // More aggressive scaling for narrow screens to keep title compact
                        DifficultyHeader(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            scale = if (isNarrow) 0.75f else 1f
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        CardPreview(modifier = Modifier.height(180.dp))
                        
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

                // Global Actions - Floating as preferred by user
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ActionIconButton(
                        icon = AppIcons.Settings,
                        contentDescription = stringResource(Res.string.settings),
                        onClick = {
                            audioService.playClick()
                            navigator.push(SettingsScreen())
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionIconButton(
                        icon = AppIcons.Info,
                        contentDescription = stringResource(Res.string.stats),
                        onClick = {
                            audioService.playClick()
                            navigator.push(StatsScreen())
                        }
                    )
                }
            }
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
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                .size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
