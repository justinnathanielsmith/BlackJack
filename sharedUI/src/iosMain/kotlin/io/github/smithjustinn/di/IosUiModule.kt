package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.AppDatabaseConstructor
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.IosAudioServiceImpl
import io.github.smithjustinn.services.IosHapticsServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

val iosUiModule =
    module {
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
        singleOf(::IosHapticsServiceImpl) { bind<HapticsService>() }
        singleOf(::IosAudioServiceImpl) { bind<AudioService>() }
        single<AppDatabase> {
            val dbFile = NSHomeDirectory() + "/memory_match.db"
            return@single Room
                .databaseBuilder<AppDatabase>(
                    name = dbFile,
                    factory = { AppDatabaseConstructor.initialize() },
                ).setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .addMigrations(
                    AppDatabase.MIGRATION_1_2,
                    AppDatabase.MIGRATION_2_3,
                    AppDatabase.MIGRATION_3_4,
                    AppDatabase.MIGRATION_4_5,
                    AppDatabase.MIGRATION_5_6,
                    AppDatabase.MIGRATION_6_7,
                ).build()
        }
    }
