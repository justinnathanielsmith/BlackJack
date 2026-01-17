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

    override suspend fun setPeekEnabled(enabled: Boolean) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(isPeekEnabled = enabled))
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        val current = dao.getSettings().firstOrNull() ?: SettingsEntity()
        dao.saveSettings(current.copy(isSoundEnabled = enabled))
    }
}
