package io.github.smithjustinn.data.repositories

import io.github.smithjustinn.data.extensions.mapToStateFlow
import io.github.smithjustinn.data.local.SettingsDao
import io.github.smithjustinn.data.local.SettingsEntity
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsRepositoryImpl(
    private val dao: SettingsDao,
) : SettingsRepository {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val writeMutex = Mutex()

    private val settingsFlow =
        dao
            .getSettings()
            .shareIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                replay = 1,
            )

    override val isPeekEnabled: StateFlow<Boolean> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = true,
        ) { it?.isPeekEnabled ?: true }

    override val isSoundEnabled: StateFlow<Boolean> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = true,
        ) { it?.isSoundEnabled ?: true }

    override val isMusicEnabled: StateFlow<Boolean> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = true,
        ) { it?.isMusicEnabled ?: true }

    override val isWalkthroughCompleted: StateFlow<Boolean> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = false,
        ) { it?.isWalkthroughCompleted ?: false }

    override val soundVolume: StateFlow<Float> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = 1.0f,
        ) { it?.soundVolume ?: 1.0f }

    override val musicVolume: StateFlow<Float> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = 1.0f,
        ) { it?.musicVolume ?: 1.0f }

    override val areSuitsMultiColored: StateFlow<Boolean> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = false,
        ) { it?.areSuitsMultiColored ?: false }

    override val isThirdEyeEnabled: StateFlow<Boolean> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = false,
        ) { it?.isThirdEyeEnabled ?: false }

    override val isHeatShieldEnabled: StateFlow<Boolean> =
        settingsFlow.mapToStateFlow(
            scope = scope,
            initialValue = false,
        ) { it?.isHeatShieldEnabled ?: false }

    override suspend fun setPeekEnabled(enabled: Boolean) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(isPeekEnabled = enabled))
        }

    override suspend fun setSoundEnabled(enabled: Boolean) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(isSoundEnabled = enabled))
        }

    override suspend fun setMusicEnabled(enabled: Boolean) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(isMusicEnabled = enabled))
        }

    override suspend fun setWalkthroughCompleted(completed: Boolean) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(isWalkthroughCompleted = completed))
        }

    override suspend fun setSoundVolume(volume: Float) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(soundVolume = volume))
        }

    override suspend fun setMusicVolume(volume: Float) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(musicVolume = volume))
        }

    override suspend fun setSuitsMultiColored(enabled: Boolean) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(areSuitsMultiColored = enabled))
        }

    override suspend fun setThirdEyeEnabled(enabled: Boolean) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(isThirdEyeEnabled = enabled))
        }

    override suspend fun setHeatShieldEnabled(enabled: Boolean) =
        writeMutex.withLock {
            val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
            dao.saveSettings(current.copy(isHeatShieldEnabled = enabled))
        }
}
