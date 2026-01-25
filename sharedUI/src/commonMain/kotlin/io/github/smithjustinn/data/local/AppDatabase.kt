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
        SettingsEntity::class,
        DailyChallengeEntity::class,
    ],
    version = 7,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun gameStateDao(): GameStateDao
    abstract fun settingsDao(): SettingsDao
    abstract fun dailyChallengeDao(): DailyChallengeDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE settings ADD COLUMN cardBackTheme TEXT NOT NULL DEFAULT 'GEOMETRIC'")
                connection.execSQL("ALTER TABLE settings ADD COLUMN cardSymbolTheme TEXT NOT NULL DEFAULT 'CLASSIC'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE settings ADD COLUMN areSuitsMultiColored INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_challenges (date INTEGER NOT NULL, isCompleted INTEGER NOT NULL, score INTEGER NOT NULL, timeSeconds INTEGER NOT NULL, moves INTEGER NOT NULL, PRIMARY KEY(date))",
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
