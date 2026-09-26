package com.mymusic.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mymusic.app.core.common.Constants
import com.mymusic.app.data.repository.ArtistRepository
import com.mymusic.app.data.repository.UserRepository
import com.mymusic.app.domain.model.Artist
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val needsRoleSelection: Boolean = false,
    val currentUser: User? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                when (val result = userRepository.getUserById(user.uid)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isAuthenticated = true,
                            currentUser = result.data
                        )
                    }
                    else -> {
                        // User doc doesn't exist → needs role selection
                        _uiState.value = _uiState.value.copy(needsRoleSelection = true)
                    }
                }
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No UID returned")
                val user = User(
                    id = uid,
                    displayName = name,
                    email = email,
                    role = Constants.ROLE_LISTENER,
                    createdAt = System.currentTimeMillis()
                )
                userRepository.createUser(user)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    needsRoleSelection = true,
                    currentUser = user
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No UID")
                when (val userResult = userRepository.getUserById(uid)) {
                    is Result.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = userResult.data
                    )
                    else -> _uiState.value = _uiState.value.copy(isLoading = false, needsRoleSelection = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: throw Exception("No user")
                val isNewUser = result.additionalUserInfo?.isNewUser == true
                if (isNewUser) {
                    val user = User(
                        id = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        role = Constants.ROLE_LISTENER,
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        createdAt = System.currentTimeMillis()
                    )
                    userRepository.createUser(user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        needsRoleSelection = true,
                        currentUser = user
                    )
                } else {
                    when (val userResult = userRepository.getUserById(firebaseUser.uid)) {
                        is Result.Success -> _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = userResult.data
                        )
                        else -> _uiState.value = _uiState.value.copy(isLoading = false, needsRoleSelection = true)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectRole(role: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.updateUser(uid, mapOf("role" to role))
            if (role == Constants.ROLE_ARTIST) {
                val currentUser = _uiState.value.currentUser
                artistRepository.createOrUpdateArtist(
                    Artist(
                        id = uid,
                        name = currentUser?.displayName ?: "",
                        photoUrl = currentUser?.photoUrl
                    )
                )
            }
            val updatedUser = (_uiState.value.currentUser ?: User(id = uid)).copy(role = role)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isAuthenticated = true,
                needsRoleSelection = false,
                currentUser = updatedUser
            )
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
