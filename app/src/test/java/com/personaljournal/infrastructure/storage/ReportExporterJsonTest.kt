package com.personaljournal.infrastructure.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.ExportRequest
import com.personaljournal.domain.model.FactorCategory
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodFactor
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.Tag
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * JVM/Robolectric test to ensure JSON export includes core fields.
 */
@RunWith(RobolectricTestRunner::class)
class ReportExporterJsonTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun exportJson_contains_core_fields() {
        runBlocking {
            val exporter = ReportExporter(context)
            val now = Clock.System.now()
            val entries = listOf(
                JournalEntry(
                    id = 1L,
                title = "First",
                content = "Body",
                createdAt = now,
                updatedAt = now,
                mood = Mood(id = 1L, label = "Calm", level = MoodLevel.GOOD, emoji = "🙂", colorHex = "#00FF00"),
                prompt = null,
                tags = listOf(Tag(id = 1L, label = "Work")),
                media = listOf(MediaAttachment(id = "m1", type = AttachmentType.PHOTO, filePath = "/tmp/a.jpg")),
                secondaryEmotions = listOf("Calm"),
                factors = listOf(MoodFactor(id = "f1", label = "Sleep", category = FactorCategory.HABIT))
            )
        )

        val request = ExportRequest(
            range = ExportRange.Last30Days,
            includeMedia = true,
            format = ExportFormat.JSON
        )
        val file = exporter.export(entries, request)

        val json = Json.parseToJsonElement(file.readText()).jsonArray.first().jsonObject
        assertEquals("First", json["title"]?.jsonPrimitive?.content)
        assertEquals("Calm", json["mood"]?.jsonPrimitive?.content)
        assertEquals("GOOD", json["moodLevel"]?.jsonPrimitive?.content)
        assertTrue(json["tags"]?.jsonArray?.any { it.jsonPrimitive.content == "Work" } == true)
        assertTrue(json["media"]?.jsonArray?.isNotEmpty() == true)

        file.delete()
        }
    }
}
