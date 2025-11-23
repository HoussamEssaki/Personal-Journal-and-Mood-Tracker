package com.personaljournal.data.migration

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.personaljournal.data.local.room.MIGRATION_3_4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Migration smoke test v3 -> v4 (adds isPinned column).
 * We create a v3 schema manually, then open Room with migration.
 */
@RunWith(RobolectricTestRunner::class)
class Migration3To4Test {

    @Test
    fun migrate_addsIsPinnedColumn() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val dbName = "migration-test"
        context.deleteDatabase(dbName)

        // Create v3 schema manually (without isPinned) and seed one row.
        val helperFactory = FrameworkSQLiteOpenHelperFactory()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS journal_entries (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            title TEXT NOT NULL,
                            content TEXT NOT NULL,
                            richTextJson TEXT NOT NULL,
                            createdAtEpoch INTEGER NOT NULL,
                            updatedAtEpoch INTEGER NOT NULL,
                            moodId INTEGER NOT NULL,
                            moodLevel TEXT NOT NULL,
                            promptId TEXT,
                            promptTitle TEXT,
                            promptDescription TEXT,
                            mediaPaths TEXT NOT NULL,
                            tags TEXT NOT NULL,
                            tagsSearchable TEXT NOT NULL,
                            secondaryEmotions TEXT NOT NULL,
                            factors TEXT NOT NULL,
                            isEncrypted INTEGER NOT NULL,
                            isSynced INTEGER NOT NULL,
                            locationLat REAL,
                            locationLon REAL,
                            locationName TEXT,
                            weatherTemp REAL,
                            weatherCondition TEXT
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO journal_entries (id, title, content, richTextJson, createdAtEpoch, updatedAtEpoch, moodId, moodLevel,
                         promptId, promptTitle, promptDescription, mediaPaths, tags, tagsSearchable, secondaryEmotions, factors,
                         isEncrypted, isSynced, locationLat, locationLon, locationName, weatherTemp, weatherCondition)
                         VALUES (1, 't', 'c', '{}', 0, 0, 0, 'NEUTRAL', null, null, null, '[]', '[]', '', '[]', '[]',
                         0, 0, null, null, null, null, null)
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        val writableDb = helperFactory.create(config).writableDatabase

        // Run the migration directly on the SupportSQLiteDatabase.
        MIGRATION_3_4.migrate(writableDb)

        writableDb.query("PRAGMA table_info(journal_entries)").use { cursor ->
            var found = false
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                if (name == "isPinned") {
                    found = true
                    break
                }
            }
            assertTrue("isPinned column should exist after migration", found)
        }
        writableDb.close()
    }
}
