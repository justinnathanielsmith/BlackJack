package io.github.smithjustinn.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import io.github.smithjustinn.theme.StartBackgroundBottom
import io.github.smithjustinn.theme.StartBackgroundTop
import io.github.smithjustinn.ui.game.GameScreen
import io.github.smithjustinn.ui.settings.SettingsScreen
import io.github.smithjustinn.ui.start.components.DifficultySelectionSection
import io.github.smithjustinn.ui.start.components.StartHeader
import io.github.smithjustinn.ui.stats.StatsScreen

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(StartBackgroundTop, StartBackgroundBottom)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp), // Restored padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Step 2: Implement the Header (Contains Title and Tilted Cards)
                StartHeader()

                Spacer(modifier = Modifier.height(48.dp)) // Restored airy spacing

                // Steps 3, 4, 5: Difficulty Selector, Game Mode Selector, and Action Buttons
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
                    onSettingsClick = {
                        audioService.playClick()
                        navigator.push(SettingsScreen())
                    },
                    onStatsClick = {
                        audioService.playClick()
                        navigator.push(StatsScreen())
                    }
                )
            }
        }
    }
}
