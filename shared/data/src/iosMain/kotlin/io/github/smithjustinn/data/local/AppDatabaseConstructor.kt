package io.github.smithjustinn.data.local

import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT")
actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase = throw NotImplementedError("Implemented by KSP")
}
