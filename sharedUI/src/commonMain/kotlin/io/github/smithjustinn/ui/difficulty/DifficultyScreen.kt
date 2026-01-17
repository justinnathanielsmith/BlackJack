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

        LaunchedEffect(Unit) {
            screenModel.handleIntent(DifficultyIntent.CheckSavedGame)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Top Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        graph.audioService.playClick()
                        navigator.push(SettingsScreen())
                    }) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = stringResource(Res.string.settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        graph.audioService.playClick()
                        navigator.push(StatsScreen())
                    }) {
                        Icon(
                            imageVector = AppIcons.Info,
                            contentDescription = stringResource(Res.string.stats),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DifficultyHeader()

                // Slightly smaller preview to ensure fit on smaller screens
                CardPreview(modifier = Modifier.heightIn(max = 160.dp))

                DifficultySelectionSection(
                    state = state,
                    onDifficultySelected = { level ->
                        graph.audioService.playClick()
                        screenModel.handleIntent(DifficultyIntent.SelectDifficulty(level))
                    },
                    onModeSelected = { mode ->
                        graph.audioService.playClick()
                        screenModel.handleIntent(DifficultyIntent.SelectMode(mode))
                    },
                    onStartGame = {
                        graph.audioService.playClick()
                        screenModel.handleIntent(
                            DifficultyIntent.StartGame(
                                state.selectedDifficulty.pairs,
                                state.selectedMode
                            )
                        ) { pairs, mode ->
                            navigator.push(GameScreen(pairs, forceNewGame = true, mode = mode))
                        }
                    },
                    onResumeGame = {
                        graph.audioService.playClick()
                        screenModel.handleIntent(DifficultyIntent.ResumeGame) { pairs, mode ->
                            navigator.push(GameScreen(pairs, forceNewGame = false, mode = mode))
                        }
                    }
                )
            }
        }
    }
}
