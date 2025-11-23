package com.personaljournal.infrastructure.storage

import android.content.Context
import com.personaljournal.infrastructure.security.crypto.AesCipher
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class EncryptedMediaStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aesCipher: AesCipher
) {
    private val mediaDir: File by lazy {
        File(context.filesDir, "media").apply { mkdirs() }
    }

    suspend fun save(bytes: ByteArray, extension: String): File = withContext(Dispatchers.IO) {
        val payload = aesCipher.encrypt(bytes)
        val file = File(mediaDir, "${System.currentTimeMillis()}.$extension")
        file.outputStream().use { output ->
            output.write(payload.iv.size)
            output.write(payload.iv)
            output.write(payload.data)
        }
        file
    }

    suspend fun read(file: File): ByteArray = withContext(Dispatchers.IO) {
        file.inputStream().use { input ->
            val ivSize = input.read()
            val iv = ByteArray(ivSize)
            var offset = 0
            while (offset < ivSize) {
                val read = input.read(iv, offset, ivSize - offset)
                if (read == -1) break
                offset += read
            }
            val encrypted = input.readBytes()
            aesCipher.decrypt(AesCipher.CipherPayload(iv, encrypted))
        }
    }

    suspend fun delete(file: File) = withContext(Dispatchers.IO) {
        if (file.exists()) file.delete()
    }

    suspend fun decryptToTemp(file: File): File = withContext(Dispatchers.IO) {
        val output = File(context.cacheDir, "${file.nameWithoutExtension}.tmp")
        output.writeBytes(read(file))
        output
    }

    fun fileFromPath(path: String): File = File(path)
}
