package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.JvmAudioServiceImpl
import io.github.smithjustinn.services.JvmHapticsServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

val jvmUiModule =
    module {
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
        singleOf(::JvmHapticsServiceImpl) { bind<HapticsService>() }
        singleOf(::JvmAudioServiceImpl) { bind<AudioService>() }
        single<AppDatabase> {
            val dbFile = File(System.getProperty("user.home"), ".memory_match.db")
            Room
                .databaseBuilder<AppDatabase>(
                    name = dbFile.absolutePath,
                ).setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
