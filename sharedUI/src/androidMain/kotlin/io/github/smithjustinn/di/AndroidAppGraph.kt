package io.github.smithjustinn.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.di.modules.DataModule
import io.github.smithjustinn.services.AndroidAudioServiceImpl
import io.github.smithjustinn.services.AndroidHapticsServiceImpl
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import kotlinx.coroutines.Dispatchers

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        DataModule::class,
    ]
)
interface AndroidAppGraph : AppGraph {
    @Provides fun provideApplicationContext(application: Application): Context = application

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides application: Application,
        ): AndroidAppGraph
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideHapticsService(impl: AndroidHapticsServiceImpl): HapticsService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideAudioService(impl: AndroidAudioServiceImpl): AudioService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(context: Context): AppDatabase {
        val dbFile = context.getDatabasePath("memory_match.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }
}

fun createAndroidGraph(application: Application): AppGraph = createGraphFactory<AndroidAppGraph.Factory>().create(application)
