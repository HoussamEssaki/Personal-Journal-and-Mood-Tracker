package com.personaljournal.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.personaljournal.data.local.room.dao.JournalEntryDao
import com.personaljournal.data.local.room.entity.JournalEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Lightweight DAO checks for pin/unpin ordering and toggle behavior.
 */
@RunWith(RobolectricTestRunner::class)
class JournalEntryDaoTest {

    private lateinit var db: PersonalJournalDatabase
    private lateinit var dao: JournalEntryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PersonalJournalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.journalEntryDao()
    }

    @After
    fun tearDown() {
        if (this::db.isInitialized) {
            db.close()
        }
    }

    @Test
    fun pinned_entries_sorted_first_then_recent() = runBlocking {
        val base = sampleEntry(createdAt = 1, updatedAt = 1)
        val id1 = dao.insert(base.copy(title = "old", createdAtEpoch = 1, updatedAtEpoch = 1))
        val id2 = dao.insert(base.copy(title = "recent", createdAtEpoch = 3, updatedAtEpoch = 3))
        val id3 = dao.insert(base.copy(title = "mid", createdAtEpoch = 2, updatedAtEpoch = 2, isPinned = true))

        val snapshot = dao.snapshot()

        // Expect pinned entry first, then newest by createdAt desc.
        assertEquals(listOf(id3, id2, id1), snapshot.map { it.entry.id })
        assertTrue(snapshot.first().entry.isPinned)
        assertFalse(snapshot[1].entry.isPinned)
        assertFalse(snapshot[2].entry.isPinned)
    }

    @Test
    fun setPinned_toggles_flag() = runBlocking {
        val id = dao.insert(sampleEntry(createdAt = 10, updatedAt = 10))

        dao.setPinned(id, true)
        assertTrue(dao.getById(id)?.entry?.isPinned == true)

        dao.setPinned(id, false)
        assertFalse(dao.getById(id)?.entry?.isPinned == true)
    }

    @Test
    fun search_filters_by_mood_tag_media_and_date() = runBlocking {
        val base = sampleEntry(createdAt = 50, updatedAt = 50)
        val withMedia = dao.insert(
            base.copy(
                title = "withMedia",
                createdAtEpoch = 100,
                updatedAtEpoch = 100,
                moodLevel = "GOOD",
                mediaPaths = listOf("a"),
                tags = listOf("work"),
                tagsSearchable = "work"
            )
        )
        dao.insert(
            base.copy(
                title = "noMedia",
                createdAtEpoch = 110,
                updatedAtEpoch = 110,
                moodLevel = "GOOD",
                mediaPaths = emptyList(),
                tags = listOf("work"),
                tagsSearchable = "work"
            )
        )
        dao.insert(
            base.copy(
                title = "otherTag",
                createdAtEpoch = 120,
                updatedAtEpoch = 120,
                moodLevel = "GOOD",
                mediaPaths = listOf("b"),
                tags = listOf("life"),
                tagsSearchable = "life"
            )
        )

        // Filter by tag and mood without media constraint should return two entries with tag "work".
        val tagFiltered = dao.search(
            query = null,
            startEpoch = 80,
            endEpoch = 130,
            moodLevels = null,
            hasMedia = null,
            tagsFilter = "work"
        ).first()

        assertEquals(2, tagFiltered.size)

        // Now require media + tag to narrow to one.
        val mediaFiltered = dao.search(
            query = null,
            startEpoch = 80,
            endEpoch = 130,
            moodLevels = null,
            hasMedia = true,
            tagsFilter = "work"
        ).first()

        assertEquals(listOf(withMedia), mediaFiltered.map { it.entry.id })
    }

    private fun sampleEntry(createdAt: Long, updatedAt: Long): JournalEntryEntity =
        JournalEntryEntity(
            title = "t",
            content = "c",
            richTextJson = "{}",
            createdAtEpoch = createdAt,
            updatedAtEpoch = updatedAt,
            moodId = 0,
            moodLevel = "NEUTRAL",
            promptId = null,
            promptTitle = null,
            promptDescription = null,
            mediaPaths = emptyList(),
            tags = emptyList(),
            tagsSearchable = "",
            secondaryEmotions = emptyList(),
            factors = emptyList(),
            isEncrypted = false,
            isSynced = false,
            locationLat = null,
            locationLon = null,
            locationName = null,
            weatherTemp = null,
            weatherCondition = null
        )
}
