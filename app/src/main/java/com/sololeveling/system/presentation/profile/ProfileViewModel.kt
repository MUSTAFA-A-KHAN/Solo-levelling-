package com.sololeveling.system.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _playerState = MutableStateFlow<Player?>(null)
    val playerState: StateFlow<Player?> = _playerState.asStateFlow()

    private val _authUser = MutableStateFlow<FirebaseUser?>(null)
    val authUser: StateFlow<FirebaseUser?> = _authUser.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepository.getPlayer().collectLatest { player ->
                _playerState.value = player
            }
        }

        viewModelScope.launch {
            authRepository.authState.collectLatest { user ->
                _authUser.value = user
            }
        }
    }

    fun getSignInIntent() = authRepository.getGoogleSignInIntent()

    suspend fun handleSignInResult(data: android.content.Intent?) =
        authRepository.handleSignInResult(data).isSuccess

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
