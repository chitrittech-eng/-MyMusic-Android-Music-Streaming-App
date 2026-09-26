package com.mymusic.app.feature.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var player: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionJob: Job? = null

    init {
        initController()
    }

    private fun initController() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            player = controllerFuture?.get()
            player?.addListener(playerListener)
            startPositionUpdates()
        }, CoroutineScope(Dispatchers.Main).asExecutor())
    }

    private fun CoroutineScope.asExecutor() = java.util.concurrent.Executor {
        launch { it.run() }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        override fun onPlaybackStateChanged(state: Int) {
            _isBuffering.value = state == Player.STATE_BUFFERING
            if (state == Player.STATE_READY) {
                _duration.value = player?.duration ?: 0L
            }
        }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentSong()
        }
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffled.value = shuffleModeEnabled
        }
        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }
    }

    private fun updateCurrentSong() {
        val item = player?.currentMediaItem ?: return
        val metadata = item.mediaMetadata
        _currentSong.value = Song(
            id = item.mediaId,
            title = metadata.title?.toString() ?: "",
            artistName = metadata.artist?.toString() ?: "",
            albumTitle = metadata.albumTitle?.toString(),
            coverUrl = metadata.artworkUri?.toString(),
            audioUrl = item.localConfiguration?.uri?.toString() ?: ""
        )
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                _currentPosition.value = player?.currentPosition ?: 0L
                delay(500)
            }
        }
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        _queue.update { queue }
        val mediaItems = queue.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id)
                .setUri(s.audioUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artistName)
                        .setAlbumTitle(s.albumTitle)
                        .setArtworkUri(s.coverUrl?.let { android.net.Uri.parse(it) })
                        .build()
                )
                .build()
        }
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player?.apply {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }
    }

    fun playPause() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() { player?.seekToNextMediaItem() }
    fun previous() { player?.seekToPreviousMediaItem() }

    fun seekTo(positionMs: Long) { player?.seekTo(positionMs) }

    fun toggleShuffle() {
        val newShuffle = !(player?.shuffleModeEnabled ?: false)
        player?.shuffleModeEnabled = newShuffle
    }

    fun cycleRepeatMode() {
        val next = when (player?.repeatMode ?: Player.REPEAT_MODE_OFF) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player?.repeatMode = next
    }

    fun addToQueue(song: Song) {
        val newQueue = _queue.value.toMutableList().apply { add(song) }
        _queue.update { newQueue }
        player?.addMediaItem(
            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(song.audioUrl)
                .build()
        )
    }

    fun release() {
        positionJob?.cancel()
        scope.cancel()
        MediaController.releaseFuture(controllerFuture!!)
    }
}
