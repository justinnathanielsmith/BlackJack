package io.github.smithjustinn.di

import androidx.compose.runtime.staticCompositionLocalOf
import co.touchlab.kermit.Logger
import io.github.smithjustinn.ui.start.StartScreenModel
import io.github.smithjustinn.ui.game.GameScreenModel
import io.github.smithjustinn.ui.stats.StatsScreenModel
import io.github.smithjustinn.ui.settings.SettingsScreenModel
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService

/**
 * The primary entry point for the dependency graph.
 * This interface only exposes what is needed by the UI layer.
 */
interface AppGraph {
    val logger: Logger
    val startScreenModel: StartScreenModel
    val gameScreenModel: GameScreenModel
    val statsScreenModel: StatsScreenModel
    val settingsScreenModel: SettingsScreenModel
    val audioService: AudioService
    val hapticsService: HapticsService
}

val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("No AppGraph provided")
}
