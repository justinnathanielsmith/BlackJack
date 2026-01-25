package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.AppDatabaseConstructor
import io.github.smithjustinn.di.modules.DataModule
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.IosAudioServiceImpl
import io.github.smithjustinn.services.IosHapticsServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSHomeDirectory

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        DataModule::class,
    ],
)
interface IosAppGraph : AppGraph {
    @Provides
    @SingleIn(AppScope::class)
    fun provideApplicationScope(): kotlinx.coroutines.CoroutineScope =
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main)

    @Provides
    @SingleIn(AppScope::class)
    fun provideHapticsService(impl: IosHapticsServiceImpl): HapticsService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideAudioService(impl: IosAudioServiceImpl): AudioService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(): AppDatabase {
        val dbFile = NSHomeDirectory() + "/memory_match.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = { AppDatabaseConstructor.initialize() },
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
            )
            .build()
    }
}

fun createIosGraph(): AppGraph = createGraph<IosAppGraph>()
