package com.mymusic.app.feature.download

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.mymusic.app.core.data.local.DownloadedSongDao
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DownloadsUiState(val downloads: List<Song> = emptyList())

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadedSongDao: DownloadedSongDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            downloadedSongDao.getAll().collect { entities ->
                val songs = entities.map { e ->
                    Song(
                        id = e.songId,
                        title = e.title,
                        artistId = e.artistId,
                        artistName = e.artistName,
                        audioUrl = e.localAudioPath,
                        coverUrl = e.coverUrl,
                        duration = e.duration,
                        genre = e.genre
                    )
                }
                _uiState.update { it.copy(downloads = songs) }
            }
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            val request = DownloadWorker.buildRequest(
                songId = song.id,
                title = song.title,
                artistId = song.artistId,
                artistName = song.artistName,
                albumId = song.albumId,
                audioUrl = song.audioUrl,
                coverUrl = song.coverUrl,
                duration = song.duration,
                genre = song.genre
            )
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    fun deleteDownload(songId: String) {
        viewModelScope.launch {
            downloadedSongDao.delete(songId)
            val file = File(context.filesDir, "downloads/$songId.mp3")
            if (file.exists()) file.delete()
        }
    }
}
