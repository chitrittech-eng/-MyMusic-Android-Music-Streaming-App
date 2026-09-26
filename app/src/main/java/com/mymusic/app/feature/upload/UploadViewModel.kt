package com.mymusic.app.feature.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mymusic.app.core.common.Constants
import com.mymusic.app.data.repository.AgreementRepository
import com.mymusic.app.data.repository.ArtistRepository
import com.mymusic.app.data.repository.SongRepository
import com.mymusic.app.data.repository.StorageRepository
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.LicenseType
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadUiState(
    val title: String = "",
    val genre: String = "",
    val lyrics: String = "",
    val albumId: String? = null,
    val audioUri: Uri? = null,
    val coverUri: Uri? = null,
    // ── Rights & Licensing ──────────────────────────────────────────────────
    val licenseType: LicenseType = LicenseType.ARTIST_OWNED,
    val licenseSource: String = "",          // only relevant for non-artist-owned types
    val rightsCheckboxAccepted: Boolean = false,
    // ── Upload state ────────────────────────────────────────────────────────
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val storageRepository: StorageRepository,
    private val agreementRepository: AgreementRepository,
    private val artistRepository: ArtistRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    // ── UI state mutators ────────────────────────────────────────────────────

    fun updateTitle(title: String) = _uiState.update { it.copy(title = title) }
    fun updateGenre(genre: String) = _uiState.update { it.copy(genre = genre) }
    fun updateLyrics(lyrics: String) = _uiState.update { it.copy(lyrics = lyrics) }
    fun setAudioUri(uri: Uri) = _uiState.update { it.copy(audioUri = uri) }
    fun setCoverUri(uri: Uri) = _uiState.update { it.copy(coverUri = uri) }
    fun updateLicenseType(type: LicenseType) = _uiState.update { it.copy(licenseType = type, licenseSource = "") }
    fun updateLicenseSource(source: String) = _uiState.update { it.copy(licenseSource = source) }
    fun toggleRightsCheckbox() = _uiState.update { it.copy(rightsCheckboxAccepted = !it.rightsCheckboxAccepted) }

    // ── Upload ───────────────────────────────────────────────────────────────

    fun uploadSong() {
        val uid = auth.currentUser?.uid ?: return
        val state = _uiState.value

        // ── Rights gate: enforced in ViewModel so it cannot be bypassed via UI ──
        if (!state.rightsCheckboxAccepted) {
            _uiState.update {
                it.copy(error = "You must confirm you have the rights to upload this track.")
            }
            return
        }

        if (state.title.isBlank() || state.audioUri == null) {
            _uiState.update { it.copy(error = "Title and audio file are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null, uploadProgress = 0.1f) }

            val user = (userRepository.getUserById(uid) as? Result.Success)?.data
            val acceptedAt = System.currentTimeMillis()

            // Derive optional licenseSource — null for artist-owned tracks
            val licenseSource: String? = if (
                state.licenseType != LicenseType.ARTIST_OWNED && state.licenseSource.isNotBlank()
            ) state.licenseSource else null

            // ── Step 1: Create song doc in Firestore (with full rights fields) ──
            val tempSong = Song(
                artistId = uid,
                artistName = user?.displayName ?: "",
                title = state.title,
                genre = state.genre,
                lyrics = state.lyrics,
                albumId = state.albumId,
                isApproved = false,
                licenseType = state.licenseType.value,
                rightsConfirmed = true,
                rightsConfirmedAt = acceptedAt,
                agreementVersion = Constants.CURRENT_AGREEMENT_VERSION,
                licenseSource = licenseSource,
                approvalStatus = "pending"
            )
            val songIdResult = songRepository.uploadSong(tempSong)
            if (songIdResult !is Result.Success) {
                _uiState.update { it.copy(isUploading = false, error = "Failed to create song record") }
                return@launch
            }
            val songId = songIdResult.data
            _uiState.update { it.copy(uploadProgress = 0.3f) }

            // ── Step 2: Upload audio to Firebase Storage ──────────────────────
            val audioResult = storageRepository.uploadAudio(songId, state.audioUri)
            if (audioResult !is Result.Success) {
                _uiState.update { it.copy(isUploading = false, error = "Failed to upload audio file") }
                return@launch
            }
            _uiState.update { it.copy(uploadProgress = 0.6f) }

            // ── Step 3: Upload cover art if provided ──────────────────────────
            val coverUrl = state.coverUri?.let {
                (storageRepository.uploadSongCover(songId, it) as? Result.Success)?.data
            }
            _uiState.update { it.copy(uploadProgress = 0.8f) }

            // ── Step 4: Update song doc with download URLs ────────────────────
            songRepository.uploadSong(
                tempSong.copy(
                    id = songId,
                    audioUrl = audioResult.data,
                    coverUrl = coverUrl
                )
            )

            // ── Step 5: Write immutable agreement acceptance audit record ─────
            agreementRepository.recordAcceptance(
                userId = uid,
                songId = songId,
                agreementVersion = Constants.CURRENT_AGREEMENT_VERSION
            )

            // ── Step 6: Increment artist stream counters ──────────────────────
            artistRepository.incrementStreams(uid)

            _uiState.update { it.copy(isUploading = false, uploadProgress = 1f, isSuccess = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun reset() = _uiState.update { UploadUiState() }
}
