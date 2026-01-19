package io.github.smithjustinn.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.theme.StartBackgroundBottom
import io.github.smithjustinn.theme.StartBackgroundTop
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.game.GameScreen
import io.github.smithjustinn.ui.settings.SettingsScreen
import io.github.smithjustinn.ui.start.components.CardPreview
import io.github.smithjustinn.ui.start.components.DifficultySelectionSection
import io.github.smithjustinn.ui.start.components.StartHeader
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                GlobalActionsRow(
                    modifier = Modifier.fillMaxWidth(),
                    onSettingsClick = {
                        audioService.playClick()
                        navigator.push(SettingsScreen())
                    },
                    onStatsClick = {
                        audioService.playClick()
                        navigator.push(StatsScreen())
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                StartHeader()

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
