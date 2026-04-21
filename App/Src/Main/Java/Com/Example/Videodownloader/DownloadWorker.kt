package com.example.videodownloader

import android.content.Context
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val url = inputData.getString("url") ?: return@withContext Result.failure()
        val format = inputData.getString("format") ?: "best"

        val request = YoutubeDLRequest(url)
        request.addOption("-f", format)
        
        // Android Scoped Storage compliant path (Downloads folder)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputTemplate = "${downloadsDir.absolutePath}/%(title)s.%(ext)s"
        request.addOption("-o", outputTemplate)

        // For Audio-only we extract to mp3 using ffmpeg
        if (format == "bestaudio") {
            request.addOption("--extract-audio")
            request.addOption("--audio-format", "mp3")
        }

        try {
            YoutubeDL.getInstance().execute(request, id.toString()) { progress, _, _ ->
                // Send progress back to the UI
                setProgressAsync(workDataOf("progress" to progress.toInt()))
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(workDataOf("error" to (e.message ?: "Unknown Error")))
        }
    }
}
