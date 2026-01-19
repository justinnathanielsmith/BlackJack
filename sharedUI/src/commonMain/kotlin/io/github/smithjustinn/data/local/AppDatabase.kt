package io.github.smithjustinn.data.local

import androidx.room.*
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
    version = 4
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
                connection.execSQL("ALTER TABLE settings ADD COLUMN isWalkthroughCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE settings ADD COLUMN isMusicEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE settings ADD COLUMN soundVolume REAL NOT NULL DEFAULT 1.0")
                connection.execSQL("ALTER TABLE settings ADD COLUMN musicVolume REAL NOT NULL DEFAULT 1.0")
            }
        }
    }
}

// The Room compiler generates the actual implementation.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
