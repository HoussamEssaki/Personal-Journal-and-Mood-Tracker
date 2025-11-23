package com.personaljournal.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration history:
 * v3->v4 introduces the isPinned column on journal_entries.
 * Earlier versions are no-ops (schema unchanged), kept to avoid destructive fallback.
 */

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No schema change recorded between 1 and 2; keep placeholder for forward migration.
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No schema change recorded between 2 and 3; keep placeholder for forward migration.
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE journal_entries
            ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}
