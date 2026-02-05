package io.github.smithjustinn.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.services.AndroidAudioServiceImpl
import io.github.smithjustinn.services.AndroidHapticsServiceImpl
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val androidUiModule =
    module {
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
        singleOf(::AndroidHapticsServiceImpl) { bind<HapticsService>() }
        singleOf(::AndroidAudioServiceImpl) { bind<AudioService>() }
        single<AppDatabase> {
            val context = get<Context>()
            val dbFile = context.getDatabasePath("memory_match.db")
            Room
                .databaseBuilder<AppDatabase>(
                    context = context,
                    name = dbFile.absolutePath,
                ).setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
