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
    exportSchema = true,
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
        const val DATABASE_VERSION = 5

        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE player_economy ADD COLUMN selectedThemeId TEXT NOT NULL DEFAULT 'GEOMETRIC'",
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("DROP TABLE IF EXISTS `settings`")
                    connection.execSQL(
                        "CREATE TABLE IF NOT EXISTS `settings` (" +
                            "`id` INTEGER NOT NULL, " +
                            "`isPeekEnabled` INTEGER NOT NULL, " +
                            "`isSoundEnabled` INTEGER NOT NULL, " +
                            "`isMusicEnabled` INTEGER NOT NULL, " +
                            "`isWalkthroughCompleted` INTEGER NOT NULL, " +
                            "`soundVolume` REAL NOT NULL, " +
                            "`musicVolume` REAL NOT NULL, " +
                            "PRIMARY KEY(`id`))",
                    )
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(connection: SQLiteConnection) {
                    // Renaming columns in player_economy
                    // SQLite does not support renaming columns directly in older versions,
                    // but modern SQLite does.
                    // However, Room often expects us to recreated the table or use
                    // ALTER TABLE RENAME COLUMN if supported.
                    // Let's use ALTER TABLE RENAME COLUMN which is supported in SQLite 3.25.0+ (Android 10+, iOS 13+).
                    // Since we are targeting newer versions, this should be fine.
                    connection.execSQL("ALTER TABLE player_economy RENAME COLUMN unlockedItemIds TO owned_items")
                    connection.execSQL("ALTER TABLE player_economy RENAME COLUMN selectedThemeId TO active_theme")
                    connection.execSQL("ALTER TABLE player_economy RENAME COLUMN selectedSkinId TO active_skin")
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE player_economy ADD COLUMN active_music TEXT NOT NULL DEFAULT 'music_default'",
                    )
                    connection.execSQL(
                        "ALTER TABLE player_economy ADD COLUMN active_powerup TEXT NOT NULL DEFAULT 'powerup_none'",
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
