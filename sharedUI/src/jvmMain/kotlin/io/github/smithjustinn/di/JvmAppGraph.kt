package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.di.modules.DataModule
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.JvmAudioServiceImpl
import io.github.smithjustinn.services.JvmHapticsServiceImpl
import kotlinx.coroutines.Dispatchers
import java.io.File

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        DataModule::class,
    ]
)
interface JvmAppGraph : AppGraph {
    @Provides
    @SingleIn(AppScope::class)
    fun provideHapticsService(impl: JvmHapticsServiceImpl): HapticsService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideAudioService(impl: JvmAudioServiceImpl): AudioService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(): AppDatabase {
        val dbFile = File(System.getProperty("user.home"), ".memory_match.db")
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
        .build()
    }
}

fun createJvmGraph(): AppGraph = createGraph<JvmAppGraph>()
