package com.mymusic.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mymusic.app.data.repository.PlaylistRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.Playlist
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = true,
    val playlists: List<Playlist> = emptyList(),
    val likedSongs: List<Song> = emptyList(),
    val selectedFilter: LibraryFilter = LibraryFilter.ALL
)

enum class LibraryFilter { ALL, PLAYLISTS, ALBUMS, ARTISTS }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        val uid = auth.currentUser?.uid ?: return
        observePlaylists(uid)
    }

    private fun observePlaylists(uid: String) {
        viewModelScope.launch {
            playlistRepository.getUserPlaylistsFlow(uid).collect { playlists ->
                _uiState.update { it.copy(isLoading = false, playlists = playlists) }
            }
        }
    }

    fun setFilter(filter: LibraryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun createPlaylist(name: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val user = (userRepository.getUserById(uid) as? Result.Success)?.data
            playlistRepository.createPlaylist(
                Playlist(
                    ownerId = uid,
                    ownerName = user?.displayName ?: "",
                    name = name,
                    isPublic = true
                )
            )
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
        }
    }
}
