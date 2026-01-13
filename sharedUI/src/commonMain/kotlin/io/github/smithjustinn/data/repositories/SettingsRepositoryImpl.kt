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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Inject
class SettingsRepositoryImpl(
    private val dao: SettingsDao
) : SettingsRepository {
    
    // Using a dedicated scope for the StateFlow to ensure it lives as long as the repository
    private val scope = CoroutineScope(Dispatchers.IO)

    override val isPeekEnabled: StateFlow<Boolean> = dao.getSettings()
        .map { it?.isPeekEnabled ?: true }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    override suspend fun setPeekEnabled(enabled: Boolean) {
        dao.saveSettings(SettingsEntity(isPeekEnabled = enabled))
    }
}
