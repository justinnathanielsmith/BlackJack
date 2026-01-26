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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.StartBackgroundBottom
import io.github.smithjustinn.theme.StartBackgroundTop
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

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(StartBackgroundTop, StartBackgroundBottom),
                        ),
                ),
    ) {
        StartMainContent(state, component, audioService)
    }
}

@Composable
private fun StartMainContent(
    state: DifficultyState,
    component: StartComponent,
    audioService: AudioService,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        StartHeader(
            settings = state.cardSettings,
        )

        Spacer(modifier = Modifier.height(48.dp))

        DifficultySelectionSection(
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
        )
    }
}
