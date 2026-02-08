package io.github.smithjustinn.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService

@Composable
fun GameEventHandler(
    component: GameComponent,
    onTheNuts: () -> Unit,
) {
    val currentOnTheNuts by rememberUpdatedState(onTheNuts)
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    val hapticsService = graph.hapticsService

    LaunchedEffect(Unit) {
        audioService.startMusic()
        component.events.collect { event ->
            handleGameEvent(event, audioService, hapticsService, currentOnTheNuts)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioService.stopMusic()
        }
    }
}

private fun handleGameEvent(
    event: GameUiEvent,
    audioService: AudioService,
    hapticsService: HapticsService,
    onTheNuts: () -> Unit,
) {
    when (event) {
        GameUiEvent.PlayFlip -> audioService.playEffect(AudioService.SoundEffect.FLIP)
        GameUiEvent.PlayMatch -> audioService.playEffect(AudioService.SoundEffect.MATCH)
        GameUiEvent.PlayMismatch -> audioService.playEffect(AudioService.SoundEffect.MISMATCH)
        GameUiEvent.PlayTheNuts -> {
            audioService.playEffect(AudioService.SoundEffect.THE_NUTS)
            onTheNuts()
        }
        GameUiEvent.PlayWin -> {
            audioService.stopMusic()
            audioService.playEffect(AudioService.SoundEffect.WIN)
        }
        GameUiEvent.PlayLose -> {
            audioService.stopMusic()
            audioService.playEffect(AudioService.SoundEffect.LOSE)
        }
        GameUiEvent.PlayHighScore -> audioService.playEffect(AudioService.SoundEffect.HIGH_SCORE)
        GameUiEvent.PlayDeal -> audioService.playEffect(AudioService.SoundEffect.DEAL)
        GameUiEvent.VibrateMatch -> hapticsService.vibrateMatch()
        GameUiEvent.VibrateMismatch -> hapticsService.vibrateMismatch()
        GameUiEvent.VibrateTick -> hapticsService.vibrateTick()
        GameUiEvent.VibrateWarning -> hapticsService.vibrateWarning()
        GameUiEvent.VibrateHeat -> hapticsService.vibrateHeat()
    }
}
