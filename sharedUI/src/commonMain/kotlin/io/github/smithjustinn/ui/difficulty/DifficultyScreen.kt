package io.github.smithjustinn.ui.difficulty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

        Box(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Actions - Moved statusBarsPadding here to avoid double spacing with SpaceEvenly
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            audioService.playClick()
                            navigator.push(SettingsScreen())
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                    ) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = stringResource(Res.string.settings),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            audioService.playClick()
                            navigator.push(StatsScreen())
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                    ) {
                        Icon(
                            imageVector = AppIcons.Info,
                            contentDescription = stringResource(Res.string.stats),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    DifficultyHeader()

                    CardPreview(modifier = Modifier.heightIn(max = 180.dp))

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
                }
            }
        }
    }
}
