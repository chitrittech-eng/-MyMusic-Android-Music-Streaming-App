package com.mymusic.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mymusic.app.data.repository.ArtistRepository
import com.mymusic.app.data.repository.SongRepository
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

data class DashboardUiState(
    val isLoading: Boolean = true,
    val artist: Artist? = null,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val artist = (artistRepository.getArtistById(uid) as? Result.Success)?.data
            val songs = (songRepository.getSongsByArtist(uid) as? Result.Success)?.data ?: emptyList()
            _uiState.update { it.copy(isLoading = false, artist = artist, songs = songs) }
        }
    }

    fun refresh() = loadDashboard()
}
