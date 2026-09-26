package com.mymusic.app.feature.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mymusic.app.data.repository.AlbumRepository
import com.mymusic.app.data.repository.ArtistRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.domain.model.Album
import com.mymusic.app.domain.model.Artist
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistUiState(
    val isLoading: Boolean = true,
    val artist: Artist? = null,
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isFollowing: Boolean = false
)

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    private var currentArtistId: String = ""

    fun loadArtist(artistId: String) {
        currentArtistId = artistId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val artistResult = artistRepository.getArtistById(artistId)
            val songsResult = songRepository.getSongsByArtist(artistId)
            val albumsResult = albumRepository.getAlbumsByArtist(artistId)

            val artist = (artistResult as? Result.Success)?.data
            val songs = (songsResult as? Result.Success)?.data ?: emptyList()
            val albums = (albumsResult as? Result.Success)?.data ?: emptyList()

            val currentUserId = auth.currentUser?.uid
            val isFollowing = artist?.followers?.contains(currentUserId) == true

            _uiState.update {
                it.copy(
                    isLoading = false,
                    artist = artist,
                    topSongs = songs.sortedByDescending { s -> s.plays }.take(10),
                    albums = albums,
                    isFollowing = isFollowing
                )
            }
        }
    }

    fun toggleFollow() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val isFollowing = _uiState.value.isFollowing
            if (isFollowing) {
                artistRepository.unfollowArtist(uid, currentArtistId)
            } else {
                artistRepository.followArtist(uid, currentArtistId)
            }
            _uiState.update { it.copy(isFollowing = !isFollowing) }
        }
    }
}
