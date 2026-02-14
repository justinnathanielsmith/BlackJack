package io.github.smithjustinn.domain.repositories

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val isPeekEnabled: StateFlow<Boolean>
    val isSoundEnabled: StateFlow<Boolean>
    val isMusicEnabled: StateFlow<Boolean>
    val isWalkthroughCompleted: StateFlow<Boolean>
    val soundVolume: StateFlow<Float>
    val musicVolume: StateFlow<Float>
    val areSuitsMultiColored: StateFlow<Boolean>
    val isThirdEyeEnabled: StateFlow<Boolean>

    suspend fun setPeekEnabled(enabled: Boolean)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setMusicEnabled(enabled: Boolean)

    suspend fun setWalkthroughCompleted(completed: Boolean)

    suspend fun setSoundVolume(volume: Float)

    suspend fun setMusicVolume(volume: Float)

    suspend fun setSuitsMultiColored(enabled: Boolean)

    suspend fun setThirdEyeEnabled(enabled: Boolean)
}
