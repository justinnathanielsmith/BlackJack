package io.github.smithjustinn.data.local

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualMigrationTest {
    @Test
    fun testMigration3To4() =
        runTest {
            val driver = BundledSQLiteDriver()
            val connection = driver.open(":memory:")

            try {
                // 1. Setup V3 Schema for player_economy
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `player_economy` (" +
                        "`id` INTEGER NOT NULL, " +
                        "`balance` INTEGER NOT NULL, " +
                        "`unlockedItemIds` TEXT NOT NULL, " +
                        "`selectedThemeId` TEXT NOT NULL, " +
                        "`selectedSkinId` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))",
                )

                // 2. Insert test data
                connection.execSQL(
                    "INSERT INTO player_economy (id, balance, unlockedItemIds, selectedThemeId, selectedSkinId) " +
                        "VALUES (0, 5000, 'theme_standard,skin_classic,bonus_item', 'theme_standard', 'skin_classic')",
                )

                // 3. Run Migration 3 -> 4
                AppDatabase.MIGRATION_3_4.migrate(connection)

                // 4. Verify Schema and Data
                val statement = connection.prepare("SELECT * FROM player_economy WHERE id = 0")
                try {
                    assertTrue(statement.step(), "Row should exist")

                    // Verify the data was preserved and is accessible via new column names
                    // Column indices: id (0), balance (1), owned_items (2), active_theme (3), active_skin (4)
                    // in the order of ALTER TABLE RENAME COLUMN?
                    // Wait, RENAME COLUMN keeps the order.

                    assertEquals(0L, statement.getLong(0))
                    assertEquals(5000L, statement.getLong(1))
                    assertEquals("theme_standard,skin_classic,bonus_item", statement.getText(2))
                    assertEquals("theme_standard", statement.getText(3))
                    assertEquals("skin_classic", statement.getText(4))

                    // Double check by querying with names to ensure name change took effect
                    val checkNames =
                        connection.prepare(
                            "SELECT owned_items, active_theme, active_skin FROM player_economy",
                        )
                    try {
                        assertTrue(checkNames.step(), "Should be able to query new column names")
                        assertEquals("theme_standard,skin_classic,bonus_item", checkNames.getText(0))
                        assertEquals("theme_standard", checkNames.getText(1))
                        assertEquals("skin_classic", checkNames.getText(2))
                    } finally {
                        checkNames.close()
                    }
                } finally {
                    statement.close()
                }
            } finally {
                connection.close()
            }
        }
}
