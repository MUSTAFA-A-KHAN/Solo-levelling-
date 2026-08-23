package com.sololeveling.system.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.LeaderboardRepository
import com.sololeveling.system.domain.repository.PlayerRepository
import com.sololeveling.system.domain.repository.QuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val authRepository: AuthRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _playerState = MutableStateFlow<Player?>(null)
    val playerState: StateFlow<Player?> = _playerState.asStateFlow()

    private val _authUser = MutableStateFlow<FirebaseUser?>(null)
    val authUser: StateFlow<FirebaseUser?> = _authUser.asStateFlow()

    private val _nameEditState = MutableStateFlow<NameEditState>(NameEditState.Idle)
    val nameEditState: StateFlow<NameEditState> = _nameEditState.asStateFlow()

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

    fun updatePlayerName(newName: String) {
        viewModelScope.launch {
            val player = _playerState.value ?: return@launch
            if (newName.isBlank() || newName == player.name) {
                _nameEditState.value = NameEditState.Idle
                return@launch
            }

            _nameEditState.value = NameEditState.Saving
            val updatedPlayer = player.copy(name = newName.trim())
            playerRepository.updatePlayer(updatedPlayer)

            val completedQuests = questRepository.getCompletedQuests().firstOrNull()?.size ?: 0
            leaderboardRepository.updateMyEntry(updatedPlayer, completedQuests)

            _nameEditState.value = NameEditState.Success
        }
    }

    fun dismissNameEditResult() {
        _nameEditState.value = NameEditState.Idle
    }

    sealed class NameEditState {
        object Idle : NameEditState()
        object Saving : NameEditState()
        object Success : NameEditState()
    }
}
