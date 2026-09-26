package com.mymusic.app.feature.settings

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mymusic.app.core.common.Constants
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val audioQuality: Int = 1,
    val crossfadeDuration: Int = 0,
    val normalizeVolume: Boolean = false,
    val privateSession: Boolean = false,
    val userRole: String = "listener"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("mymusic_prefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(loadPrefs())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserRole()
    }

    private fun loadPrefs() = SettingsUiState(
        audioQuality = prefs.getInt(Constants.PREF_AUDIO_QUALITY, 1),
        crossfadeDuration = prefs.getInt(Constants.PREF_CROSSFADE_DURATION, 0),
        normalizeVolume = prefs.getBoolean(Constants.PREF_NORMALIZE_VOLUME, false),
        privateSession = prefs.getBoolean(Constants.PREF_PRIVATE_SESSION, false)
    )

    private fun loadUserRole() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val user = (userRepository.getUserById(uid) as? Result.Success)?.data
            _uiState.update { it.copy(userRole = user?.role ?: "listener") }
        }
    }

    fun setAudioQuality(index: Int) {
        prefs.edit { putInt(Constants.PREF_AUDIO_QUALITY, index) }
        _uiState.update { it.copy(audioQuality = index) }
    }

    fun setCrossfade(seconds: Int) {
        prefs.edit { putInt(Constants.PREF_CROSSFADE_DURATION, seconds) }
        _uiState.update { it.copy(crossfadeDuration = seconds) }
    }

    fun setNormalizeVolume(enabled: Boolean) {
        prefs.edit { putBoolean(Constants.PREF_NORMALIZE_VOLUME, enabled) }
        _uiState.update { it.copy(normalizeVolume = enabled) }
    }

    fun setPrivateSession(enabled: Boolean) {
        prefs.edit { putBoolean(Constants.PREF_PRIVATE_SESSION, enabled) }
        _uiState.update { it.copy(privateSession = enabled) }
    }

    fun signOut() {
        auth.signOut()
    }
}
