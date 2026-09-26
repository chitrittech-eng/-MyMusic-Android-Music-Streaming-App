package com.mymusic.app.feature.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mymusic.app.core.common.Constants
import com.mymusic.app.core.data.local.DownloadedSongEntity
import com.mymusic.app.core.data.local.DownloadedSongDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadedSongDao: DownloadedSongDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val songId = inputData.getString("songId") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()
        val artistId = inputData.getString("artistId") ?: ""
        val artistName = inputData.getString("artistName") ?: ""
        val albumId = inputData.getString("albumId")
        val audioUrl = inputData.getString("audioUrl") ?: return Result.failure()
        val coverUrl = inputData.getString("coverUrl")
        val duration = inputData.getLong("duration", 0L)
        val genre = inputData.getString("genre") ?: ""

        return try {
            val dir = File(applicationContext.filesDir, "downloads")
            dir.mkdirs()
            val file = File(dir, "$songId.mp3")

            val connection = URL(audioUrl).openConnection() as HttpURLConnection
            connection.connect()
            connection.inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            downloadedSongDao.insert(
                DownloadedSongEntity(
                    songId = songId,
                    title = title,
                    artistId = artistId,
                    artistName = artistName,
                    albumId = albumId,
                    localAudioPath = file.absolutePath,
                    coverUrl = coverUrl,
                    duration = duration,
                    genre = genre
                )
            )
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun buildRequest(
            songId: String,
            title: String,
            artistId: String,
            artistName: String,
            albumId: String?,
            audioUrl: String,
            coverUrl: String?,
            duration: Long,
            genre: String
        ): OneTimeWorkRequest {
            val data = workDataOf(
                "songId" to songId,
                "title" to title,
                "artistId" to artistId,
                "artistName" to artistName,
                "albumId" to albumId,
                "audioUrl" to audioUrl,
                "coverUrl" to coverUrl,
                "duration" to duration,
                "genre" to genre
            )
            return OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(data)
                .addTag(Constants.WORK_TAG_DOWNLOAD)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        }
    }
}
