package io.github.smithjustinn.di

import android.app.Activity
import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.services.AdService
import io.github.smithjustinn.services.AndroidAdService
import io.github.smithjustinn.services.AndroidAudioServiceImpl
import io.github.smithjustinn.services.AndroidHapticsServiceImpl
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Android-specific providers for the Metro dependency graph.
 * Replaces the old androidUiModule.
 */
@ContributesTo(AppScope::class)
interface AndroidProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @SingleIn(AppScope::class)
    @Provides
    fun provideHapticsService(context: Context): HapticsService = AndroidHapticsServiceImpl(context)

    @SingleIn(AppScope::class)
    @Provides
    fun provideAudioService(
        context: Context,
        logger: Logger,
        settingsRepository: SettingsRepository,
    ): AudioService = AndroidAudioServiceImpl(context, logger, settingsRepository)

    @SingleIn(AppScope::class)
    @Provides
    fun provideAdService(activity: Activity?): AdService =
        AndroidAdService(activityProvider = { activity })

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        val dbFile = context.getDatabasePath("memory_match.db")
        return Room
            .databaseBuilder<AppDatabase>(
                context = context,
                name = dbFile.absolutePath,
            ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .build()
    }
}
