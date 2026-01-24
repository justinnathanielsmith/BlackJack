package io.github.smithjustinn.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun createTestDatabase(): AppDatabase = Room.inMemoryDatabaseBuilder<AppDatabase>(
    factory = { AppDatabaseConstructor.initialize() },
)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
