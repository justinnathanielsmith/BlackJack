package io.github.smithjustinn.data.repositories

import io.github.smithjustinn.data.local.SettingsDao
import io.github.smithjustinn.data.local.SettingsEntity
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

    override val areSuitsMultiColored: StateFlow<Boolean> =
        settingsFlow
            .map { it?.areSuitsMultiColored ?: false }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

    override val isThirdEyeEnabled: StateFlow<Boolean> =
        settingsFlow
            .map { it?.isThirdEyeEnabled ?: false }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

    override val isHeatShieldEnabled: StateFlow<Boolean> =
        settingsFlow
            .map { it?.isHeatShieldEnabled ?: false }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

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
