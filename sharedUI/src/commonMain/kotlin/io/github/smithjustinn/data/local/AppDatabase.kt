package io.github.smithjustinn.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [
        GameStatsEntity::class,
        LeaderboardEntity::class,
        GameStateEntity::class,
        SettingsEntity::class
    ],
    version = 5
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun gameStateDao(): GameStateDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `leaderboard` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `pairCount` INTEGER NOT NULL, `score` INTEGER NOT NULL, `timeSeconds` INTEGER NOT NULL, `moves` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `saved_game_state` (`id` INTEGER NOT NULL, `gameStateJson` TEXT NOT NULL, `elapsedTimeSeconds` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `settings` (`id` INTEGER NOT NULL, `isPeekEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }
    }
}

// The Room compiler generates the actual implementation.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
