package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.AppDatabaseConstructor
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.services.AdService
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.IosAdService
import io.github.smithjustinn.services.IosAudioServiceImpl
import io.github.smithjustinn.services.IosHapticsServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import platform.Foundation.NSHomeDirectory

/**
 * iOS-specific providers for the Metro dependency graph.
 * Replaces the old iosUiModule.
 */
@ContributesTo(AppScope::class)
interface IosProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @SingleIn(AppScope::class)
    @Provides
    fun provideHapticsService(): HapticsService = IosHapticsServiceImpl()

    @SingleIn(AppScope::class)
    @Provides
    fun provideAudioService(
        logger: Logger,
        settingsRepository: SettingsRepository,
    ): AudioService = IosAudioServiceImpl(logger, settingsRepository)

    @SingleIn(AppScope::class)
    @Provides
    fun provideAdService(): AdService = IosAdService()

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDatabase(): AppDatabase {
        val dbFile = NSHomeDirectory() + "/memory_match.db"
        return Room
            .databaseBuilder<AppDatabase>(
                name = dbFile,
                factory = { AppDatabaseConstructor.initialize() },
            ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .build()
    }
}
