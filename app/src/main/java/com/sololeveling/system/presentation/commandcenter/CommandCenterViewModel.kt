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

import com.sololeveling.system.data.health.HealthConnectManager
import com.sololeveling.system.domain.usecase.ProgressionEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.LocalDate
import java.time.ZoneId

@HiltViewModel
class CommandCenterViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val progressionEngine: ProgressionEngine,
    val healthConnectManager: HealthConnectManager
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

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun syncHealthData() {
        viewModelScope.launch {
            val currentPlayer = _playerState.value ?: return@launch

            if (!healthConnectManager.hasAllPermissions()) {
                _uiEvent.emit(UiEvent.RequestHealthPermissions(healthConnectManager.requiredPermissions))
                return@launch
            }

            // Sync data since the last tracked sync.
            // If the user has 0 XP (just installed before the fix), fallback to the start of today
            // so they don't miss out on today's earlier steps.
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val since = if (currentPlayer.xp == 0L && currentPlayer.lastSyncTime > startOfDay) {
                startOfDay
            } else {
                currentPlayer.lastSyncTime
            }

            val now = System.currentTimeMillis()

            val steps = healthConnectManager.getRecentSteps(since)
            val workoutMinutes = healthConnectManager.getRecentWorkoutDurationMinutes(since)

            val updatedPlayer = progressionEngine.processHealthData(currentPlayer, steps, workoutMinutes, now)

            playerRepository.updatePlayer(updatedPlayer)
        }
    }

    sealed class UiEvent {
        data class RequestHealthPermissions(val permissions: Set<String>) : UiEvent()
    }
}
