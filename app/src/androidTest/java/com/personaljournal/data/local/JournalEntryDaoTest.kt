package com.personaljournal.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.personaljournal.data.local.room.PersonalJournalDatabase
import com.personaljournal.data.local.room.entity.JournalEntryEntity
import com.personaljournal.data.local.room.entity.MoodEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalEntryDaoTest {

    private lateinit var db: PersonalJournalDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PersonalJournalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runBlocking {
            db.moodDao().insertAll(
                listOf(
                    MoodEntity(1, "Happy", "GOOD", "😊", "#7ED321")
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndSearchEntries() = runBlocking {
        val entity = JournalEntryEntity(
            title = "Day One",
            content = "It was great",
            richTextJson = "[]",
            createdAtEpoch = 1,
            updatedAtEpoch = 1,
            moodId = 1,
            moodLevel = "GOOD",
            promptId = null,
            promptTitle = null,
            promptDescription = null,
            mediaPaths = emptyList(),
            tags = listOf("#test"),
            tagsSearchable = "#test",
            secondaryEmotions = emptyList(),
            factors = emptyList(),
            isEncrypted = true,
            isSynced = false,
            locationLat = null,
            locationLon = null,
            locationName = null,
            weatherTemp = null,
            weatherCondition = null
        )
        db.journalEntryDao().insert(entity)

        val result = db.journalEntryDao().search(
            query = "great",
            startEpoch = null,
            endEpoch = null,
            moodLevels = listOf("GOOD"),
            hasMedia = null,
            tagsFilter = "#test"
        ).first()

        assertEquals(1, result.size)
    }
}
