package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.data.local.SettingsDao
import io.github.smithjustinn.data.local.SettingsEntity
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

@Inject
class SettingsRepositoryImpl(
    private val dao: SettingsDao,
    private val logger: Logger,
) : SettingsRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val settingsFlow =
        dao
            .getSettings()
            .shareIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                replay = 1,
            )

    override val isPeekEnabled: StateFlow<Boolean> =
        settingsFlow
            .map { it?.isPeekEnabled ?: true }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = true,
            )

    override val isSoundEnabled: StateFlow<Boolean> =
        settingsFlow
            .map { it?.isSoundEnabled ?: true }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = true,
            )

    override val isMusicEnabled: StateFlow<Boolean> =
        settingsFlow
            .map { it?.isMusicEnabled ?: true }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = true,
            )

    override val isWalkthroughCompleted: StateFlow<Boolean> =
        settingsFlow
            .map { it?.isWalkthroughCompleted ?: false }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

    override val soundVolume: StateFlow<Float> =
        settingsFlow
            .map { it?.soundVolume ?: 1.0f }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = 1.0f,
            )

    override val musicVolume: StateFlow<Float> =
        settingsFlow
            .map { it?.musicVolume ?: 1.0f }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = 1.0f,
            )

    @Suppress("TooGenericExceptionCaught")
    override val cardBackTheme: StateFlow<CardBackTheme> =
        settingsFlow
            .map { entity ->
                try {
                    CardBackTheme.valueOf(entity?.cardBackTheme ?: CardBackTheme.GEOMETRIC.name)
                } catch (e: IllegalArgumentException) {
                    logger.e(e) { "Invalid CardBackTheme values stored: ${entity?.cardBackTheme}" }
                    CardBackTheme.GEOMETRIC
                } catch (e: Exception) {
                    logger.e(e) { "Failed to parse CardBackTheme: ${entity?.cardBackTheme}" }
                    CardBackTheme.GEOMETRIC
                }
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = CardBackTheme.GEOMETRIC,
            )

    @Suppress("TooGenericExceptionCaught")
    override val cardSymbolTheme: StateFlow<CardSymbolTheme> =
        settingsFlow
            .map { entity ->
                try {
                    CardSymbolTheme.valueOf(entity?.cardSymbolTheme ?: CardSymbolTheme.CLASSIC.name)
                } catch (e: IllegalArgumentException) {
                    logger.e(e) { "Invalid CardSymbolTheme values stored: ${entity?.cardSymbolTheme}" }
                    CardSymbolTheme.CLASSIC
                } catch (e: Exception) {
                    logger.e(e) { "Failed to parse CardSymbolTheme: ${entity?.cardSymbolTheme}" }
                    CardSymbolTheme.CLASSIC
                }
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = CardSymbolTheme.CLASSIC,
            )

    override val areSuitsMultiColored: StateFlow<Boolean> =
        settingsFlow
            .map { it?.areSuitsMultiColored ?: false }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

    override suspend fun setPeekEnabled(enabled: Boolean) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(isPeekEnabled = enabled))
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(isSoundEnabled = enabled))
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(isMusicEnabled = enabled))
    }

    override suspend fun setWalkthroughCompleted(completed: Boolean) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(isWalkthroughCompleted = completed))
    }

    override suspend fun setSoundVolume(volume: Float) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(soundVolume = volume))
    }

    override suspend fun setMusicVolume(volume: Float) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(musicVolume = volume))
    }

    override suspend fun setCardBackTheme(theme: CardBackTheme) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(cardBackTheme = theme.name))
    }

    override suspend fun setCardSymbolTheme(theme: CardSymbolTheme) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(cardSymbolTheme = theme.name))
    }

    override suspend fun setSuitsMultiColored(enabled: Boolean) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(areSuitsMultiColored = enabled))
    }
}
