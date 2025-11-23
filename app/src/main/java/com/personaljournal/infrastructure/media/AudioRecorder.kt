package com.personaljournal.infrastructure.media

import android.media.MediaRecorder
import android.os.Build
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun start(): File {
        stop()
        val file = File(context.cacheDir, "memo_${System.currentTimeMillis()}.m4a")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        currentFile = file
        return file
    }

    fun stop(): File? {
        recorder?.apply {
            try {
                stop()
            } catch (_: Exception) {
            } finally {
                release()
            }
        }
        recorder = null
        return currentFile
    }
}
