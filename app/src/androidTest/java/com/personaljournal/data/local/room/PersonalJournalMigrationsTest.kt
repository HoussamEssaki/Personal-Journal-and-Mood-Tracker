package com.personaljournal.data.local.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PersonalJournalMigrationsTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PersonalJournalDatabase::class.java,
        emptyList<androidx.room.migration.AutoMigrationSpec>(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate3To4_addsIsPinnedDefaultZero() {
        // Create schema at version 3.
        helper.createDatabase(TEST_DB, 3).apply {
            // Insert a row without isPinned column (pre-v4).
            execSQL(
                """
                INSERT INTO journal_entries (id, title, content, created_at, updated_at, mood, favorite, audio_path)
                VALUES (1, 't', 'c', 0, 0, 'NEUTRAL', 0, null)
                """
            )
            close()
        }

        // Run migration to v4.
        val db: SupportSQLiteDatabase = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

        // Query the new column to ensure default applied on migrated DB.
        db.query("SELECT isPinned FROM journal_entries WHERE id=1").use { cursor ->
            require(cursor.moveToFirst())
            val pinned = cursor.getInt(0)
            assert(pinned == 0) { "Expected default isPinned=0 after migration" }
        }
    }

    companion object {
        private const val TEST_DB = "pjmigrations.db"
    }
}
