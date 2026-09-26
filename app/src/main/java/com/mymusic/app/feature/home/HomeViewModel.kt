package com.mymusic.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymusic.app.core.data.local.RecentlyPlayedDao
import com.mymusic.app.core.data.local.RecentlyPlayedEntity
import com.mymusic.app.data.repository.CategoryRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.Category
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import com.mymusic.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "Good morning",
    val currentUser: User? = null,
    val recentlyPlayed: List<Song> = emptyList(),
    val newReleases: List<Song> = emptyList(),
    val featuredSongs: List<Song> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val recentlyPlayedDao: RecentlyPlayedDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = getGreeting()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
        observeCurrentUser()
        observeRecentlyPlayed()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUserFlow().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            recentlyPlayedDao.getAll().collect { entities ->
                val songs = entities.map { e ->
                    Song(
                        id = e.songId,
                        title = e.title,
                        artistName = e.artistName,
                        coverUrl = e.coverUrl,
                        audioUrl = e.audioUrl,
                        duration = e.duration,
                        genre = e.genre
                    )
                }
                _uiState.update { it.copy(recentlyPlayed = songs) }
            }
        }
    }

    private fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val newReleasesResult = songRepository.getNewReleases(20)
            val categoriesResult = categoryRepository.getCategories()

            // Also collect featured from the live songs flow (top 10 by plays)
            songRepository.getApprovedSongsFlow()
                .take(1)
                .collect { allSongs ->
                    val featured = allSongs.sortedByDescending { it.plays }.take(10)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            featuredSongs = featured,
                            newReleases = if (newReleasesResult is Result.Success) newReleasesResult.data else emptyList(),
                            categories = if (categoriesResult is Result.Success) categoriesResult.data else emptyList()
                        )
                    }
                }
        }
    }

    fun refresh() = loadHome()

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    suspend fun addToRecentlyPlayed(song: Song) {
        recentlyPlayedDao.insert(
            RecentlyPlayedEntity(
                songId = song.id,
                title = song.title,
                artistName = song.artistName,
                coverUrl = song.coverUrl,
                audioUrl = song.audioUrl,
                duration = song.duration,
                genre = song.genre
            )
        )
        recentlyPlayedDao.trimToLimit()
    }
}
