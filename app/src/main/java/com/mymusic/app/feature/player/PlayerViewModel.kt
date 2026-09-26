package com.mymusic.app.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.PlayerState
import com.mymusic.app.domain.model.RepeatMode
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.media3.common.Player as Media3Player

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicController: MusicController,
    private val songRepository: SongRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = combine(
        musicController.currentSong,
        musicController.queue,
        musicController.isPlaying,
        musicController.currentPosition,
        musicController.duration,
        musicController.isShuffled,
        musicController.repeatMode,
        musicController.isBuffering
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val song = arr[0] as? Song
        @Suppress("UNCHECKED_CAST")
        val queue = arr[1] as List<Song>
        val isPlaying = arr[2] as Boolean
        val position = arr[3] as Long
        val duration = arr[4] as Long
        val isShuffled = arr[5] as Boolean
        val repeatModeInt = arr[6] as Int
        val isBuffering = arr[7] as Boolean

        val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
        val repeatMode = when (repeatModeInt) {
            Media3Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Media3Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
        PlayerState(
            currentSong = song,
            queue = queue,
            isPlaying = isPlaying,
            progress = progress,
            currentPositionMs = position,
            durationMs = duration,
            repeatMode = repeatMode,
            isShuffled = isShuffled,
            isBuffering = isBuffering
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerState())

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        musicController.playSong(song, queue)
        trackPlay(song)
    }

    fun playPause() = musicController.playPause()
    fun next() = musicController.next()
    fun previous() = musicController.previous()
    fun seekTo(positionMs: Long) = musicController.seekTo(positionMs)
    fun toggleShuffle() = musicController.toggleShuffle()
    fun cycleRepeat() = musicController.cycleRepeatMode()
    fun addToQueue(song: Song) = musicController.addToQueue(song)

    fun checkLiked(songId: String) {
        viewModelScope.launch {
            val uid = userRepository.currentUserId ?: return@launch
            _isLiked.value = songRepository.isLiked(uid, songId)
        }
    }

    fun toggleLike(songId: String) {
        val uid = userRepository.currentUserId ?: return
        viewModelScope.launch {
            if (_isLiked.value) {
                songRepository.unlikeSong(uid, songId)
                _isLiked.value = false
            } else {
                songRepository.likeSong(uid, songId)
                _isLiked.value = true
            }
        }
    }

    private fun trackPlay(song: Song) {
        viewModelScope.launch {
            songRepository.incrementPlayCount(song.id)
        }
    }
}
