package com.mymusic.app.feature.moderator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymusic.app.data.repository.ReportRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.Report
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModeratorUiState(
    val isLoading: Boolean = true,
    val reports: List<Report> = emptyList(),
    val pendingSongs: List<Song> = emptyList()
)

@HiltViewModel
class ModeratorViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val songRepository: SongRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeratorUiState())
    val uiState: StateFlow<ModeratorUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val reports = (reportRepository.getPendingReports() as? Result.Success)?.data ?: emptyList()
            val songs = (songRepository.getPendingApprovals() as? Result.Success)?.data ?: emptyList()
            _uiState.update { it.copy(isLoading = false, reports = reports, pendingSongs = songs) }
        }
    }

    fun resolveReport(reportId: String, resolution: String) {
        viewModelScope.launch {
            if (resolution == "banned") {
                val report = _uiState.value.reports.find { it.id == reportId }
                report?.let { userRepository.banUser(it.reportedBy) }
            }
            reportRepository.resolveReport(reportId, resolution)
            _uiState.update { it.copy(reports = it.reports.filter { r -> r.id != reportId }) }
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
            songRepository.rejectSong(songId)
            _uiState.update { it.copy(pendingSongs = it.pendingSongs.filter { s -> s.id != songId }) }
        }
    }
}
