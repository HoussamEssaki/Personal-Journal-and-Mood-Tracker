package com.personaljournal.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.personaljournal.data.local.room.entity.EntryTagCrossRef
import com.personaljournal.data.local.room.entity.JournalEntryEntity
import com.personaljournal.data.local.room.entity.JournalEntryWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Transaction
    @Query("SELECT * FROM journal_entries ORDER BY isPinned DESC, createdAtEpoch DESC")
    fun observeEntries(): Flow<List<JournalEntryWithRelations>>

    @Transaction
    @Query("SELECT * FROM journal_entries ORDER BY isPinned DESC, createdAtEpoch DESC")
    suspend fun snapshot(): List<JournalEntryWithRelations>

    @Transaction
    @Query("SELECT * FROM journal_entries WHERE isSynced = 0")
    suspend fun pendingSync(): List<JournalEntryWithRelations>

    @Transaction
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getById(id: Long): JournalEntryWithRelations?

    @Transaction
    @Query(
        """
        SELECT * FROM journal_entries
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        AND (:startEpoch IS NULL OR createdAtEpoch >= :startEpoch)
        AND (:endEpoch IS NULL OR createdAtEpoch <= :endEpoch)
        AND (:moodLevels IS NULL OR moodLevel IN (:moodLevels))
        AND (:hasMedia IS NULL OR (CASE WHEN :hasMedia = 1 THEN mediaPaths IS NOT NULL AND length(mediaPaths) > 2 ELSE 1 END))
        AND (:tagsFilter IS NULL OR tagsSearchable LIKE '%' || :tagsFilter || '%')
        ORDER BY isPinned DESC, createdAtEpoch DESC
        """
    )
    fun search(
        query: String?,
        startEpoch: Long?,
        endEpoch: Long?,
        moodLevels: List<String>?,
        hasMedia: Boolean?,
        tagsFilter: String?
    ): Flow<List<JournalEntryWithRelations>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntryEntity): Long

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<EntryTagCrossRef>)

    @Query("DELETE FROM entry_tag_cross_ref WHERE entryId = :entryId")
    suspend fun clearCrossRefs(entryId: Long)

    @Query("UPDATE journal_entries SET isPinned = :pinned WHERE id = :entryId")
    suspend fun setPinned(entryId: Long, pinned: Boolean)
}
