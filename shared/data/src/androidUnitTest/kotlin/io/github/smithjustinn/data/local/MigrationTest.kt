package io.github.smithjustinn.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import io.github.smithjustinn.data.local.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            // Database has schema version 3. Insert some data using SQL queries.
            // Using implicit 'id'=0 which is default validation for our entity
            execSQL("INSERT INTO player_economy (id, balance, unlockedItemIds, selectedThemeId, selectedSkinId) VALUES (0, 1000, 'theme_1,skin_1', 'theme_1', 'skin_1')")
            close()
        }

        // Re-open the database with version 4 and provide the migration process.
        db = helper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true,
            AppDatabase.MIGRATION_3_4
        )

        // MigrationTestHelper automatically verifies the schema changes.
        // Now let's verify the data was preserved.
        val cursor = db.query("SELECT * FROM player_economy WHERE id = 0")
        assertTrue(cursor.moveToFirst())

        // Verify column values exist with new names
        val ownedItemsIndex = cursor.getColumnIndex("owned_items")
        val activeThemeIndex = cursor.getColumnIndex("active_theme")
        val activeSkinIndex = cursor.getColumnIndex("active_skin")
        val balanceIndex = cursor.getColumnIndex("balance")

        assertTrue(ownedItemsIndex >= 0)
        assertTrue(activeThemeIndex >= 0)
        assertTrue(activeSkinIndex >= 0)

        assertEquals("theme_1,skin_1", cursor.getString(ownedItemsIndex))
        assertEquals("theme_1", cursor.getString(activeThemeIndex))
        assertEquals("skin_1", cursor.getString(activeSkinIndex))
        assertEquals(1000, cursor.getLong(balanceIndex))
        
        cursor.close()
    }
}
