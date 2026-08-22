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
import com.sololeveling.system.domain.usecase.QuestGenerator
import com.sololeveling.system.domain.repository.QuestRepository
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.usecase.QuestSyncUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.LocalDate
import java.time.ZoneId

data class DailyHealthData(
    val steps: Long = 0,
    val workoutMinutes: Long = 0,
    val caloriesBurned: Double = 0.0,
    val sleepMinutes: Long = 0
)

@HiltViewModel
class CommandCenterViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val questRepository: QuestRepository,
    private val progressionEngine: ProgressionEngine,
    private val questGenerator: QuestGenerator,
    private val questSyncUseCase: QuestSyncUseCase,
    val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _playerState = MutableStateFlow<Player?>(null)
    val playerState: StateFlow<Player?> = _playerState.asStateFlow()

    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests: StateFlow<List<Quest>> = _activeQuests.asStateFlow()

    private val _dailyHealthData = MutableStateFlow(DailyHealthData())
    val dailyHealthData: StateFlow<DailyHealthData> = _dailyHealthData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            questGenerator.checkAndGenerateQuests()
        }

        viewModelScope.launch {
            playerRepository.getPlayer().collectLatest { player ->
                _playerState.value = player
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            questRepository.getActiveQuests().collectLatest { quests ->
                _activeQuests.value = quests
            }
        }

        viewModelScope.launch {
            fetchDailyHealthData()
        }
    }

    private suspend fun fetchDailyHealthData() {
        if (!healthConnectManager.hasAllPermissions()) return

        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val steps = healthConnectManager.getRecentSteps(startOfDay)
        val workoutMinutes = healthConnectManager.getRecentWorkoutDurationMinutes(startOfDay)
        val calories = healthConnectManager.getRecentCaloriesBurned(startOfDay)
        val sleepMinutes = healthConnectManager.getRecentSleepDurationMinutes(startOfDay)

        _dailyHealthData.value = DailyHealthData(steps, workoutMinutes, calories, sleepMinutes)
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

            questSyncUseCase.syncQuestsWithHealthData(steps, workoutMinutes)

            fetchDailyHealthData() // refresh the daily total shown on the dashboard
        }
    }

    sealed class UiEvent {
        data class RequestHealthPermissions(val permissions: Set<String>) : UiEvent()
    }
}
