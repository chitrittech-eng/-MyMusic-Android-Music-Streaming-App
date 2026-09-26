package com.mymusic.app.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import com.mymusic.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = true,
    val totalUsers: Int = 0,
    val totalSongs: Int = 0,
    val pendingSongs: List<Song> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val userSearchQuery: String = ""
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAdminData()
    }

    fun loadAdminData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val users = (userRepository.getAllUsers() as? Result.Success)?.data ?: emptyList()
            val pending = (songRepository.getPendingApprovals() as? Result.Success)?.data ?: emptyList()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allUsers = users,
                    totalUsers = users.size,
                    pendingSongs = pending,
                    totalSongs = pending.size
                )
            }
        }
    }

    fun onUserSearchQueryChange(query: String) {
        _uiState.update { it.copy(userSearchQuery = query) }
    }

    fun filteredUsers(): List<User> {
        val q = _uiState.value.userSearchQuery.lowercase()
        return if (q.isBlank()) _uiState.value.allUsers
        else _uiState.value.allUsers.filter {
            it.displayName.lowercase().contains(q) || it.email.lowercase().contains(q)
        }
    }

    fun approveSong(songId: String) {
        viewModelScope.launch {
            songRepository.approveSong(songId)
            _uiState.update { it.copy(pendingSongs = it.pendingSongs.filter { s -> s.id != songId }) }
        }
    }

    fun rejectSong(songId: String) {
        viewModelScope.launch {
            songRepository.deleteSong(songId)
            _uiState.update { it.copy(pendingSongs = it.pendingSongs.filter { s -> s.id != songId }) }
        }
    }

    fun banUser(userId: String) {
        viewModelScope.launch {
            userRepository.banUser(userId)
            _uiState.update { it.copy(allUsers = it.allUsers.filter { u -> u.id != userId }) }
        }
    }

    fun promoteToModerator(userId: String) {
        viewModelScope.launch {
            userRepository.updateUserRole(userId, "moderator")
            _uiState.update {
                it.copy(allUsers = it.allUsers.map { u ->
                    if (u.id == userId) u.copy(role = "moderator") else u
                })
            }
        }
    }

    fun promoteToArtist(userId: String) {
        viewModelScope.launch {
            userRepository.updateUserRole(userId, "artist")
            _uiState.update {
                it.copy(allUsers = it.allUsers.map { u ->
                    if (u.id == userId) u.copy(role = "artist") else u
                })
            }
        }
    }
}
