package com.personaljournal.infrastructure.storage

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.model.ExportRequest
import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.Tag
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class ReportExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun export(entries: List<JournalEntry>, request: ExportRequest): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "exports").apply { mkdirs() }
            val filename = when (request.format) {
                ExportFormat.PDF -> "journal_export.pdf"
                ExportFormat.CSV -> "journal_export.csv"
                ExportFormat.JSON -> "journal_export.json"
            }
            val file = File(dir, filename)
            when (request.format) {
                ExportFormat.PDF -> exportPdf(entries, file, request.includeMedia)
                ExportFormat.CSV -> exportCsv(entries, file, request.includeMedia)
                ExportFormat.JSON -> exportJson(entries, file)
            }
            file
        }

    private fun exportPdf(entries: List<JournalEntry>, file: File, includeMedia: Boolean) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply { textSize = 12f }
        var y = 40f
        entries.forEach { entry ->
            if (y > 800f) {
                document.finishPage(page)
            }
            canvas.drawText("${entry.title} - ${entry.mood.label}", 40f, y, paint)
            y += 20f
            canvas.drawText(entry.content.take(120), 40f, y, paint)
            y += 30f
            if (includeMedia && entry.media.isNotEmpty()) {
                canvas.drawText("Attachments: ${entry.media.size}", 60f, y, paint)
                y += 20f
            }
        }
        document.finishPage(page)
        file.outputStream().use { output -> document.writeTo(output) }
        document.close()
    }

    private fun exportCsv(entries: List<JournalEntry>, file: File, includeMedia: Boolean) {
        val header = buildString {
            append("date,title,mood,tags")
            if (includeMedia) append(",mediaCount")
            append(",content\n")
        }
        file.writeText(header + entries.joinToString("\n") { entry ->
            buildString {
                append(entry.createdAt.toLocalDate())
                append(",\"${entry.title}\",")
                append(entry.mood.level.name)
                append(",\"${entry.tags.joinToString { it.label }}\"")
                if (includeMedia) append(",${entry.media.size}")
                append(",\"${entry.content.replace("\n", " ")}\"")
            }
        })
    }

    private fun exportJson(entries: List<JournalEntry>, file: File) {
        val json = Json { prettyPrint = true }
        val payload = entries.map { ExportEntryDto.from(it) }
        file.writeText(json.encodeToString(payload))
    }

    private fun kotlinx.datetime.Instant.toLocalDate(): LocalDate =
        toLocalDateTime(TimeZone.UTC).date
}

@Serializable
private data class ExportEntryDto(
    val id: Long,
    val title: String,
    val content: String,
    val createdAtIso: String,
    val updatedAtIso: String,
    val mood: String,
    val moodLevel: String,
    val prompt: String?,
    val tags: List<String>,
    val media: List<MediaDto>,
    val secondaryEmotions: List<String>,
    val factors: List<String>
) {
    companion object {
        fun from(entry: JournalEntry): ExportEntryDto = ExportEntryDto(
            id = entry.id,
            title = entry.title,
            content = entry.content,
            createdAtIso = entry.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            updatedAtIso = entry.updatedAt.toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            mood = entry.mood.label,
            moodLevel = entry.mood.level.name,
            prompt = entry.prompt?.title,
            tags = entry.tags.map(Tag::label),
            media = entry.media.map { MediaDto(it.filePath, it.type.name, it.durationSeconds) },
            secondaryEmotions = entry.secondaryEmotions,
            factors = entry.factors.map { it.label }
        )
    }
}

@Serializable
private data class MediaDto(
    val path: String,
    val type: String,
    val durationSeconds: Int?
)
