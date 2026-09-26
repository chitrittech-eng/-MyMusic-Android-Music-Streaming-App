package com.mymusic.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymusic.app.data.repository.AlbumRepository
import com.mymusic.app.data.repository.ArtistRepository
import com.mymusic.app.data.repository.CategoryRepository
import com.mymusic.app.data.repository.PlaylistRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val categories: List<Category> = emptyList(),
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val hasResults: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val playlistRepository: PlaylistRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        loadCategories()
        observeSearch()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = categoryRepository.getCategories()
            if (result is Result.Success) {
                _uiState.update { it.copy(categories = result.data) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isBlank()) {
                        _uiState.update { it.copy(
                            isSearching = false,
                            songs = emptyList(),
                            artists = emptyList(),
                            albums = emptyList(),
                            playlists = emptyList(),
                            hasResults = false
                        ) }
                    } else {
                        performSearch(q)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true) }
        val songs = (songRepository.searchSongs(query) as? Result.Success)?.data ?: emptyList()
        val artists = (artistRepository.searchArtists(query) as? Result.Success)?.data ?: emptyList()
        val albums = (albumRepository.searchAlbums(query) as? Result.Success)?.data ?: emptyList()
        val playlists = (playlistRepository.searchPlaylists(query) as? Result.Success)?.data ?: emptyList()
        _uiState.update { it.copy(
            isSearching = false,
            songs = songs,
            artists = artists,
            albums = albums,
            playlists = playlists,
            hasResults = songs.isNotEmpty() || artists.isNotEmpty() || albums.isNotEmpty() || playlists.isNotEmpty()
        ) }
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "") }
        queryFlow.value = ""
    }
}
