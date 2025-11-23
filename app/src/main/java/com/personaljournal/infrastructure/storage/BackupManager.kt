package com.personaljournal.infrastructure.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun exportDatabase(): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
        val backupFile = File(backupDir, "journal_backup.zip")
        ZipOutputStream(backupFile.outputStream()).use { zip ->
            writeDatabaseToZip(zip)
        }
        backupFile
    }

    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val output =
            context.contentResolver.openOutputStream(uri) ?: error("Unable to open destination")
        output.use { stream ->
            ZipOutputStream(stream).use { zip ->
                writeDatabaseToZip(zip)
            }
        }
    }

    suspend fun importDatabase(zipFile: File) = withContext(Dispatchers.IO) {
        zipFile.inputStream().use { restoreFromZip(it) }
    }

    suspend fun importFrom(uri: Uri) = withContext(Dispatchers.IO) {
        val input =
            context.contentResolver.openInputStream(uri) ?: error("Unable to open selected file")
        input.use { restoreFromZip(it) }
    }

    private fun writeDatabaseToZip(zip: ZipOutputStream) {
        val dbFile = File(context.getDatabasePath(DATABASE_NAME).absolutePath)
        val entry = ZipEntry(dbFile.name)
        zip.putNextEntry(entry)
        dbFile.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }

    private fun restoreFromZip(input: InputStream) {
        val target = context.getDatabasePath(DATABASE_NAME)
        ZipInputStream(BufferedInputStream(input)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    target.outputStream().use { output -> zip.copyTo(output) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "personal_journal.db"
    }
}
