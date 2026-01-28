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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.start.components.DifficultySelectionSection
import io.github.smithjustinn.ui.start.components.StartHeader

@Composable
fun StartContent(
    component: StartComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    colors.feltGreen,
                                    colors.feltGreenDark,
                                ),
                            center = androidx.compose.ui.geometry.Offset.Unspecified,
                            radius = Float.POSITIVE_INFINITY, // Smoother radial falloff
                        ),
                ),
    ) {
        StartScreenLayout(
            state = state,
            onDifficultySelected = { level ->
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onDifficultySelected(level)
            },
            onModeSelected = { mode ->
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onModeSelected(mode)
            },
            onStartGame = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onStartGame()
            },
            onResumeGame = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onResumeGame()
            },
            onSettingsClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onSettingsClick()
            },
            onStatsClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onStatsClick()
            },
            onDailyChallengeClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onDailyChallengeClick()
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun StartScreenLayout(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onDailyChallengeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PokerTheme.spacing

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        StartHeader(
            settings = state.cardSettings,
            modifier = Modifier.padding(bottom = spacing.large),
        )

        Spacer(modifier = Modifier.weight(0.3f))

        // Dealer's Tray Container
        AppCard(
            modifier = Modifier.widthIn(max = 600.dp),
        ) {
            DifficultySelectionSection(
                state = state,
                onDifficultySelected = onDifficultySelected,
                onModeSelected = onModeSelected,
                onStartGame = onStartGame,
                onResumeGame = onResumeGame,
                onSettingsClick = onSettingsClick,
                onStatsClick = onStatsClick,
                onDailyChallengeClick = onDailyChallengeClick,
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        Spacer(modifier = Modifier.height(spacing.large))
    }
}
