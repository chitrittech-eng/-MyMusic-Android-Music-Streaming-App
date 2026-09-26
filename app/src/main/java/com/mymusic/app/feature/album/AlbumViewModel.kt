package com.mymusic.app.feature.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymusic.app.data.repository.AlbumRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.domain.model.Album
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val isLoading: Boolean = true,
    val album: Album? = null,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val albumResult = albumRepository.getAlbumById(albumId)
            val album = (albumResult as? Result.Success)?.data
            val songs = if (album != null) {
                (songRepository.getSongsByAlbum(albumId) as? Result.Success)?.data ?: emptyList()
            } else emptyList()
            _uiState.update { it.copy(isLoading = false, album = album, songs = songs) }
        }
    }
}
