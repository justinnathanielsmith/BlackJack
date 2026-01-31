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
        SettingsEntity::class,
        DailyChallengeEntity::class,
    ],
    version = AppDatabase.DATABASE_VERSION,
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
        const val DATABASE_VERSION = 8
        private const val VERSION_1 = 1
        private const val VERSION_2 = 2
        private const val VERSION_3 = 3
        private const val VERSION_4 = 4
        private const val VERSION_5 = 5
        private const val VERSION_6 = 6
        private const val VERSION_7 = 7
        private const val VERSION_8 = 8

        val MIGRATION_1_2 =
            object : Migration(VERSION_1, VERSION_2) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE settings ADD COLUMN isWalkthroughCompleted INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(VERSION_2, VERSION_3) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("ALTER TABLE settings ADD COLUMN isMusicEnabled INTEGER NOT NULL DEFAULT 1")
                }
            }

        val MIGRATION_3_4 =
            object : Migration(VERSION_3, VERSION_4) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("ALTER TABLE settings ADD COLUMN soundVolume REAL NOT NULL DEFAULT 1.0")
                    connection.execSQL("ALTER TABLE settings ADD COLUMN musicVolume REAL NOT NULL DEFAULT 1.0")
                }
            }

        val MIGRATION_4_5 =
            object : Migration(VERSION_4, VERSION_5) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE settings ADD COLUMN cardBackTheme TEXT NOT NULL DEFAULT 'GEOMETRIC'",
                    )
                    connection.execSQL(
                        "ALTER TABLE settings ADD COLUMN cardSymbolTheme TEXT NOT NULL DEFAULT 'CLASSIC'",
                    )
                }
            }

        val MIGRATION_5_6 =
            object : Migration(VERSION_5, VERSION_6) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE settings ADD COLUMN areSuitsMultiColored INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }

        val MIGRATION_6_7 =
            object : Migration(VERSION_6, VERSION_7) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "CREATE TABLE IF NOT EXISTS daily_challenges " +
                            "(date INTEGER NOT NULL," +
                            " isCompleted INTEGER NOT NULL, " +
                            "score INTEGER NOT NULL, " +
                            "timeSeconds INTEGER NOT NULL, " +
                            "moves INTEGER NOT NULL, " +
                            "PRIMARY KEY(date))",
                    )
                }
            }

        val MIGRATION_7_8 =
            object : Migration(VERSION_7, VERSION_8) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "CREATE TABLE IF NOT EXISTS circuit_stats " +
                            "(runId TEXT NOT NULL, " +
                            "currentStageId INTEGER NOT NULL, " +
                            "bankedScore INTEGER NOT NULL, " +
                            "currentWager INTEGER NOT NULL, " +
                            "timestamp INTEGER NOT NULL, " +
                            "isActive INTEGER NOT NULL DEFAULT 1, " +
                            "PRIMARY KEY(runId))",
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
