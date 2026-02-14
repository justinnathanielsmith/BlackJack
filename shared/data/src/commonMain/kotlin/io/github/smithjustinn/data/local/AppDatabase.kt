package io.github.smithjustinn.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters

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

    @Suppress("MagicNumber")
    companion object {
        const val DATABASE_VERSION = 4
        val MIGRATION_3_4 =
            object : androidx.room.migration.Migration(3, 4) {
                override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
                    connection.prepare("ALTER TABLE settings ADD COLUMN isHeatShieldEnabled INTEGER NOT NULL DEFAULT 0")
                        .use { it.step() }
                }
            }
    }
}

// The Room compiler generates the actual implementation.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
