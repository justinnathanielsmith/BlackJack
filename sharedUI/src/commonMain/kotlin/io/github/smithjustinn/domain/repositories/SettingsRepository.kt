package io.github.smithjustinn.domain.repositories

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val isPeekEnabled: StateFlow<Boolean>
    suspend fun setPeekEnabled(enabled: Boolean)
}
