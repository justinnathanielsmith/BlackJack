package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.services.AdService
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.DesktopAdService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.JvmAudioServiceImpl
import io.github.smithjustinn.services.JvmHapticsServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * JVM/Desktop-specific providers for the Metro dependency graph.
 * Replaces the old jvmUiModule.
 */
@ContributesTo(AppScope::class)
interface JvmUiModule {
    @SingleIn(AppScope::class)
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @SingleIn(AppScope::class)
    @Provides
    fun provideHapticsService(): HapticsService = JvmHapticsServiceImpl()

    @SingleIn(AppScope::class)
    @Provides
    fun provideAudioService(
        logger: Logger,
        settingsRepository: SettingsRepository,
    ): AudioService = JvmAudioServiceImpl(logger, settingsRepository)

    @SingleIn(AppScope::class)
    @Provides
    fun provideAdService(): AdService = DesktopAdService()

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDatabase(): AppDatabase {
        val dbFile = File(System.getProperty("user.home"), ".memory_match.db")
        return Room
            .databaseBuilder<AppDatabase>(
                name = dbFile.absolutePath,
            ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .build()
    }
}
