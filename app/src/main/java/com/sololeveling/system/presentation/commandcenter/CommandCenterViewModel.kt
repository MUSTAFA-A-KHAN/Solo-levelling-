package com.sololeveling.system.presentation.commandcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerSyncResult
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.LeaderboardRepository
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
import com.sololeveling.system.domain.model.HealthSnapshot
import com.sololeveling.system.domain.usecase.QuestSyncUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
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
    private val authRepository: AuthRepository,
    private val leaderboardRepository: LeaderboardRepository,
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

    private val _syncUiState = MutableStateFlow<PlayerSyncUiState>(PlayerSyncUiState.Idle)
    val syncUiState: StateFlow<PlayerSyncUiState> = _syncUiState.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private var hasSynced = false

    init {
        viewModelScope.launch {
            questGenerator.checkAndGenerateQuests()
            fetchDailyHealthData()
        }

        viewModelScope.launch {
            playerRepository.getPlayer().collectLatest { player ->
                _playerState.value = player
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            questRepository.getActiveQuestsForDate(LocalDate.now().toString()).collectLatest { quests ->
                _activeQuests.value = quests
            }
        }

        viewModelScope.launch {
            authRepository.authState.collectLatest { user ->
                if (user != null && !hasSynced) {
                    hasSynced = true
                    syncPlayer(user.uid)
                }
            }
        }
    }

    private fun syncPlayer(uid: String) {
        viewModelScope.launch {
            _syncUiState.value = PlayerSyncUiState.Syncing
            _connectionStatus.value = ConnectionStatus.Syncing
            try {
                val result = playerRepository.syncWithFirestore(uid)
                questRepository.syncWithFirestore(uid)
                 _syncUiState.value = when (result) {
                    is PlayerSyncResult.Conflict -> PlayerSyncUiState.Conflict(result.remote)
                    else -> PlayerSyncUiState.Resolved
                }
                _connectionStatus.value = ConnectionStatus.Connected

                refreshLeaderboardEntry()
            } catch (e: Exception) {
                android.util.Log.e("CommandCenterViewModel", "Firestore sync failed; continuing offline", e)
                _syncUiState.value = PlayerSyncUiState.Resolved
                _connectionStatus.value = ConnectionStatus.Failed(e.message ?: "Sync failed")
            }
        }
    }

    private suspend fun refreshLeaderboardEntry(player: Player? = null) {
        val currentPlayer = player ?: playerRepository.getPlayer().firstOrNull() ?: return
        val completedQuests = questRepository.getCompletedQuests().firstOrNull()?.size ?: 0
        leaderboardRepository.updateMyEntry(currentPlayer, completedQuests)
    }

    fun confirmRemoteOverride() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUser()?.uid ?: return@launch
            try {
                playerRepository.forceDownloadFromFirestore(uid)
                _connectionStatus.value = ConnectionStatus.Connected
                refreshLeaderboardEntry()
            } catch (e: Exception) {
                android.util.Log.e("CommandCenterViewModel", "Force download from Firestore failed", e)
                _connectionStatus.value = ConnectionStatus.Failed(e.message ?: "Sync failed")
            }
            _syncUiState.value = PlayerSyncUiState.Resolved
        }
    }

    fun clearConnectionError() {
        _connectionStatus.value = ConnectionStatus.Syncing
    }

    fun dismissConflict() {
        _syncUiState.value = PlayerSyncUiState.Resolved
    }

    private suspend fun fetchDailyHealthData() {
        if (!healthConnectManager.hasAllPermissions()) return

        try {
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val snapshot = buildDailyHealthSnapshot(startOfDay)

            _dailyHealthData.value = DailyHealthData(
                steps = snapshot.steps,
                workoutMinutes = snapshot.workoutMinutes,
                caloriesBurned = healthConnectManager.getRecentCaloriesBurned(startOfDay),
                sleepMinutes = snapshot.sleepMinutes
            )

            questSyncUseCase.syncQuestsWithHealthData(snapshot)
        } catch (e: Exception) {
            android.util.Log.e("CommandCenterViewModel", "Failed to fetch daily health data", e)
        }
    }

    private suspend fun buildDailyHealthSnapshot(startOfDay: Long): HealthSnapshot {
        val eveningStart = LocalDate.now()
            .atTime(18, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val now = System.currentTimeMillis()

        return HealthSnapshot(
            steps = healthConnectManager.getRecentSteps(startOfDay),
            workoutMinutes = healthConnectManager.getRecentWorkoutDurationMinutes(startOfDay),
            sleepMinutes = healthConnectManager.getRecentSleepDurationMinutes(startOfDay),
            exerciseSessions = healthConnectManager.getRecentExerciseSessionCount(startOfDay),
            firstActivityTime = healthConnectManager.getEarliestActivityStartTime(startOfDay),
            eveningSteps = healthConnectManager.getStepsInRange(eveningStart, now)
        )
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
            try {
                val currentPlayer = _playerState.value ?: run {
                    _connectionStatus.value = ConnectionStatus.Failed("Player not initialized")
                    return@launch
                }

                if (!healthConnectManager.isHealthConnectAvailable()) {
                    _connectionStatus.value =
                        ConnectionStatus.Failed("Health Connect is not available on this device")
                    return@launch
                }

                if (!healthConnectManager.hasAllPermissions()) {
                    _uiEvent.emit(UiEvent.RequestHealthPermissions(healthConnectManager.requiredPermissions))
                    return@launch
                }

                _connectionStatus.value = ConnectionStatus.Syncing

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

                questSyncUseCase.syncQuestsWithHealthData(buildDailyHealthSnapshot(startOfDay))

                fetchDailyHealthData() // refresh the daily total shown on the dashboard
                refreshLeaderboardEntry(updatedPlayer)

                _connectionStatus.value = ConnectionStatus.Connected
            } catch (e: Exception) {
                android.util.Log.e("CommandCenterViewModel", "Health sync failed", e)
                _connectionStatus.value = ConnectionStatus.Failed(e.message ?: "Health sync failed")
            }
        }
    }

    sealed class UiEvent {
        data class RequestHealthPermissions(val permissions: Set<String>) : UiEvent()
    }

    sealed class PlayerSyncUiState {
        object Idle : PlayerSyncUiState()
        object Syncing : PlayerSyncUiState()
        data class Conflict(val remote: Player) : PlayerSyncUiState()
        object Resolved : PlayerSyncUiState()
    }

    sealed class ConnectionStatus {
        object Idle : ConnectionStatus()
        object Syncing : ConnectionStatus()
        object Connected : ConnectionStatus()
        data class Failed(val message: String) : ConnectionStatus()
    }
}
