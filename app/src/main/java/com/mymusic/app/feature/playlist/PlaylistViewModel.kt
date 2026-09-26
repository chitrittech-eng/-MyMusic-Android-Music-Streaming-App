package com.mymusic.app.feature.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mymusic.app.data.repository.PlaylistRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.domain.model.Playlist
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val isLoading: Boolean = true,
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val isOwner: Boolean = false,
    val isFollowing: Boolean = false,
    val isEditing: Boolean = false
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    private var currentPlaylistId = ""

    fun loadPlaylist(playlistId: String) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = playlistRepository.getPlaylistById(playlistId)
            if (result is Result.Success) {
                val playlist = result.data
                val uid = auth.currentUser?.uid
                val songs = playlist.songs.mapNotNull { songId ->
                    (songRepository.getSongById(songId) as? Result.Success)?.data
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        playlist = playlist,
                        songs = songs,
                        isOwner = uid == playlist.ownerId,
                        isFollowing = playlist.followers.contains(uid)
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun removeSong(songId: String) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(currentPlaylistId, songId)
            _uiState.update { it.copy(songs = it.songs.filter { s -> s.id != songId }) }
        }
    }

    fun toggleFollow() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val isFollowing = _uiState.value.isFollowing
            if (isFollowing) {
                playlistRepository.unfollowPlaylist(uid, currentPlaylistId)
            } else {
                playlistRepository.followPlaylist(uid, currentPlaylistId)
            }
            _uiState.update { it.copy(isFollowing = !isFollowing) }
        }
    }

    fun toggleEdit() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }
}
