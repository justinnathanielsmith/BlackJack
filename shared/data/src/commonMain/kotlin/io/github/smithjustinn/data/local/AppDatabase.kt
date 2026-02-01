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
        PlayerEconomyEntity::class,
    ],
    version = AppDatabase.DATABASE_VERSION,
    exportSchema = false,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao

    abstract fun leaderboardDao(): LeaderboardDao

    abstract fun gameStateDao(): GameStateDao

    abstract fun settingsDao(): SettingsDao

    abstract fun dailyChallengeDao(): DailyChallengeDao

    abstract fun playerEconomyDao(): PlayerEconomyDao

    companion object {
        const val DATABASE_VERSION = 2

        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE player_economy ADD COLUMN selectedThemeId TEXT NOT NULL DEFAULT 'GEOMETRIC'",
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
