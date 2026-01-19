package io.github.smithjustinn.data.repositories

import dev.zacsweers.metro.Inject
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
import kotlinx.coroutines.flow.stateIn

@Inject
internal class SettingsRepositoryImpl(
    private val dao: SettingsDao
) : SettingsRepository {
    
    private val scope = CoroutineScope(Dispatchers.IO)

    override val isPeekEnabled: StateFlow<Boolean> = dao.getSettings()
        .map { it?.isPeekEnabled ?: true }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    override val isSoundEnabled: StateFlow<Boolean> = dao.getSettings()
        .map { it?.isSoundEnabled ?: true }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    override val isMusicEnabled: StateFlow<Boolean> = dao.getSettings()
        .map { it?.isMusicEnabled ?: true }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    override val isWalkthroughCompleted: StateFlow<Boolean> = dao.getSettings()
        .map { it?.isWalkthroughCompleted ?: false }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override val soundVolume: StateFlow<Float> = dao.getSettings()
        .map { it?.soundVolume ?: 1.0f }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = 1.0f
        )

    override val musicVolume: StateFlow<Float> = dao.getSettings()
        .map { it?.musicVolume ?: 1.0f }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = 1.0f
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
}
