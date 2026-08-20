package com.sololeveling.system.presentation.commandcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.sololeveling.system.domain.usecase.ProgressionEngine

@HiltViewModel
class CommandCenterViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val progressionEngine: ProgressionEngine
) : ViewModel() {

    private val _playerState = MutableStateFlow<Player?>(null)
    val playerState: StateFlow<Player?> = _playerState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepository.getPlayer().collectLatest { player ->
                _playerState.value = player
                _isLoading.value = false
            }
        }
    }

    fun completeAwakening() {
        viewModelScope.launch {
            playerRepository.initializePlayer("Sung Jin-Woo")
        }
    }

    fun simulateActivity() {
        viewModelScope.launch {
            val currentPlayer = _playerState.value ?: return@launch
            // Simulate gaining 25 XP from an activity
            val updatedPlayer = progressionEngine.addXp(currentPlayer, 25)
            // Re-evaluate rank just in case
            val newRank = progressionEngine.evaluateRank(updatedPlayer)
            val finalPlayer = updatedPlayer.copy(rank = newRank)

            playerRepository.updatePlayer(finalPlayer)
        }
    }
}
