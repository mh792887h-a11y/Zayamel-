package com.example.player

import android.content.Context
import android.util.Log
import com.example.data.local.ZawamilDatabase
import com.example.data.model.Zamil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadManager(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val zamilDao = ZawamilDatabase.getDatabase(context).zamilDao()

    private val _downloadingProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadingProgress: StateFlow<Map<String, Float>> = _downloadingProgress.asStateFlow()

    companion object {
        private const val TAG = "DownloadManager"
    }

    private fun getDownloadsDir(): File {
        val dir = File(context.filesDir, "zawamil_downloads")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun downloadZamil(zamil: Zamil): Result<String> = withContext(Dispatchers.IO) {
        val targetFile = File(getDownloadsDir(), "${zamil.id}.mp3")

        if (targetFile.exists() && targetFile.length() > 0) {
            zamilDao.updateDownloadStatus(zamil.id, true, targetFile.absolutePath)
            return@withContext Result.success(targetFile.absolutePath)
        }

        try {
            _downloadingProgress.value = _downloadingProgress.value + (zamil.id to 0.05f)

            val request = Request.Builder().url(zamil.audioPath).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                _downloadingProgress.value = _downloadingProgress.value - zamil.id
                return@withContext Result.failure(Exception("فشل الاتصال بالخادم (${response.code})"))
            }

            val body = response.body!!
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        if (totalBytes > 0) {
                            val progress = (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0.05f, 0.99f)
                            _downloadingProgress.value = _downloadingProgress.value + (zamil.id to progress)
                        }
                    }
                    output.flush()
                }
            }

            _downloadingProgress.value = _downloadingProgress.value - zamil.id
            zamilDao.updateDownloadStatus(zamil.id, true, targetFile.absolutePath)
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}")
            _downloadingProgress.value = _downloadingProgress.value - zamil.id
            if (targetFile.exists()) targetFile.delete()
            Result.failure(e)
        }
    }

    suspend fun deleteDownloadedZamil(zamilId: String) = withContext(Dispatchers.IO) {
        val targetFile = File(getDownloadsDir(), "$zamilId.mp3")
        if (targetFile.exists()) {
            targetFile.delete()
        }
        zamilDao.updateDownloadStatus(zamilId, false, null)
    }

    fun isZamilDownloaded(zamilId: String): Boolean {
        val file = File(getDownloadsDir(), "$zamilId.mp3")
        return file.exists() && file.length() > 0
    }
}
