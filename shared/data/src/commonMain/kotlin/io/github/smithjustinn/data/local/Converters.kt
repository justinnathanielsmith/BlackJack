package io.github.smithjustinn.data.local

import androidx.room.TypeConverter
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilliseconds()

    @TypeConverter
    fun fromGameState(gameState: MemoryGameState?): String? =
        gameState?.let {
            json.encodeToString(it)
        }

    @TypeConverter
    fun toGameState(jsonString: String?): MemoryGameState? =
        jsonString?.let {
            json.decodeFromString(it)
        }

    companion object {
        private val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
    }
}
