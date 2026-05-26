package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloader(
    private val context: Context,
    private val modelDir: String,
    private val modelFile: String
) {
    companion object {
        private const val PREFS_NAME = "ai_model_prefs"
        private const val KEY_MODEL_READY = "model_ready"
        private const val MODEL_URL =
            "https://storage.googleapis.com/mediapipe-models/llm_inference/gemma-2b-it-q4.bin"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getModelPath(): String {
        val dir = File(context.filesDir, modelDir)
        return File(dir, modelFile).absolutePath
    }

    fun isModelExists(): Boolean {
        val file = File(getModelPath())
        return file.exists() && file.length() > 0 && prefs.getBoolean(KEY_MODEL_READY, false)
    }

    fun download(
        onProgress: (Long, Long) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val dir = File(context.filesDir, modelDir)
        if (!dir.exists()) dir.mkdirs()
        val targetFile = File(dir, modelFile)

        Thread {
            try {
                val connection = URL(MODEL_URL).openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP ${connection.responseCode}")
                }

                val totalSize = connection.contentLength.toLong()
                val input = connection.inputStream
                val output = targetFile.outputStream()

                val buffer = ByteArray(8192)
                var downloaded = 0L
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    (context as? Activity)?.runOnUiThread {
                        onProgress(downloaded, totalSize)
                    }
                }

                output.flush()
                output.close()
                input.close()
                connection.disconnect()

                prefs.edit().putBoolean(KEY_MODEL_READY, true).apply()

                (context as? Activity)?.runOnUiThread {
                    onComplete()
                }
            } catch (e: Exception) {
                targetFile.delete()
                (context as? Activity)?.runOnUiThread {
                    onError("模型下载失败：${e.message}")
                }
            }
        }.start()
    }
}
