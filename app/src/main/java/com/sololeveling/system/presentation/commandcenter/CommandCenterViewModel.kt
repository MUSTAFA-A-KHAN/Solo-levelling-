package com.sololeveling.system.presentation.commandcenter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import com.sololeveling.system.data.notifications.SystemNotificationManager
import com.sololeveling.system.data.notifications.HydrationReminderWorker
import com.sololeveling.system.domain.model.HydrationLog
import com.sololeveling.system.domain.usecase.SystemAIUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.concurrent.TimeUnit

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
    private val notificationManager: SystemNotificationManager,
    private val systemAI: SystemAIUseCase,
    val healthConnectManager: HealthConnectManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _playerState = MutableStateFlow<Player?>(null)
    val playerState: StateFlow<Player?> = _playerState.asStateFlow()

    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests: StateFlow<List<Quest>> = _activeQuests.asStateFlow()

    private val _dailyHealthData = MutableStateFlow(DailyHealthData())
    val dailyHealthData: StateFlow<DailyHealthData> = _dailyHealthData.asStateFlow()

    private val _weeklySteps = MutableStateFlow(0L)
    val weeklySteps: StateFlow<Long> = _weeklySteps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _syncUiState = MutableStateFlow<PlayerSyncUiState>(PlayerSyncUiState.Idle)
    val syncUiState: StateFlow<PlayerSyncUiState> = _syncUiState.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _aiResponse = MutableStateFlow("System: Awaiting command...")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

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
                if (player != null && player.hydrationData.currentIntakeLiters < 0.5) {
                   // Initial reminder
                   systemAI.triggerDailyEncouragement(player)
                }
                // Reschedule hydration reminders if enabled
                if (player != null && player.hydrationData.reminderEnabled) {
                    updateHydrationWork(true, player.hydrationData.reminderIntervalMinutes)
                }
            }
        }

        viewModelScope.launch {
            val currentDate = LocalDate.now()
            val weekString = "${currentDate.year}-W${currentDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())}"
            questRepository.getActiveQuestsForDate(LocalDate.now().toString(), weekString).collectLatest { quests ->
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
                    is PlayerSyncResult.Conflict -> PlayerSyncUiState.Resolved
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

    fun addHydration(liters: Double) {
        viewModelScope.launch {
            val currentPlayer = _playerState.value ?: return@launch
            val newLog = HydrationLog(amountLiters = liters)
            val updatedHydration = currentPlayer.hydrationData.copy(
                currentIntakeLiters = currentPlayer.hydrationData.currentIntakeLiters + liters,
                lastDrinkTimestamp = System.currentTimeMillis(),
                logs = currentPlayer.hydrationData.logs + newLog
            )
            val updatedPlayer = currentPlayer.copy(hydrationData = updatedHydration)
            playerRepository.updatePlayer(updatedPlayer)
            
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.showNotification(
                    "SYSTEM RECOVERY",
                    "Hydration recorded: +${liters}L. Recovery efficiency increased.",
                    notificationId = HYDRATION_LOGGED_NOTIFICATION_ID
                )
            }
            
            checkHydrationGoal(updatedPlayer)
            _aiResponse.value = systemAI.processCommand("drink water", updatedPlayer)
        }
    }

    fun resetHydration() {
        viewModelScope.launch {
            val currentPlayer = _playerState.value ?: return@launch
            val updatedHydration = currentPlayer.hydrationData.copy(
                currentIntakeLiters = 0.0,
                lastDrinkTimestamp = 0,
                logs = emptyList()
            )
            val updatedPlayer = currentPlayer.copy(hydrationData = updatedHydration)
            playerRepository.updatePlayer(updatedPlayer)
        }
    }

    fun toggleHydrationReminders(enabled: Boolean) {
        viewModelScope.launch {
            val currentPlayer = _playerState.value ?: return@launch
            val updatedHydration = currentPlayer.hydrationData.copy(reminderEnabled = enabled)
            val updatedPlayer = currentPlayer.copy(hydrationData = updatedHydration)
            playerRepository.updatePlayer(updatedPlayer)
            
            updateHydrationWork(enabled, updatedHydration.reminderIntervalMinutes)
        }
    }

    fun setHydrationReminderInterval(minutes: Int) {
        viewModelScope.launch {
            val currentPlayer = _playerState.value ?: return@launch
            val updatedHydration = currentPlayer.hydrationData.copy(reminderIntervalMinutes = minutes)
            val updatedPlayer = currentPlayer.copy(hydrationData = updatedHydration)
            playerRepository.updatePlayer(updatedPlayer)
            
            if (updatedHydration.reminderEnabled) {
                updateHydrationWork(true, minutes)
            }
        }
    }

    private fun updateHydrationWork(enabled: Boolean, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val workRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setConstraints(HydrationReminderWorker.createConstraints())
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                HydrationReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } else {
            workManager.cancelUniqueWork(HydrationReminderWorker.WORK_NAME)
        }
    }

    /**
     * Reschedules hydration reminders if they were previously enabled.
     * Should be called on app start to restore scheduled work.
     */
    fun rescheduleHydrationReminders() {
        viewModelScope.launch {
            val currentPlayer = _playerState.value ?: return@launch
            val hydration = currentPlayer.hydrationData
            if (hydration.reminderEnabled) {
                updateHydrationWork(true, hydration.reminderIntervalMinutes)
            }
        }
    }
    
    fun sendAICommand(command: String) {
        viewModelScope.launch {
            val currentPlayer = _playerState.value
            _aiResponse.value = systemAI.processCommand(command, currentPlayer)
        }
    }

    private fun checkHydrationGoal(player: Player) {
        if (player.hydrationData.currentIntakeLiters >= player.hydrationData.dailyGoalLiters) {
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.showNotification(
                    "QUEST UPDATE",
                    "Daily Hydration Goal achieved! Vitality restored.",
                    notificationId = HYDRATION_GOAL_NOTIFICATION_ID
                )
            }
        }
    }

    /**
     * Checks if notification permission is granted and emits an event if not.
     * @return true if notifications are enabled
     */
    fun checkNotificationPermission(): Boolean {
        val enabled = notificationManager.areNotificationsEnabled()
        if (!enabled) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.RequestNotificationPermission)
            }
        }
        return enabled
    }

    /**
     * Sends a test notification to verify the notification system is working.
     */
    fun sendTestNotification() {
        val success = notificationManager.showNotification(
            title = "SYSTEM TEST",
            message = "Notification system operational. All systems functioning.",
            notificationId = TEST_NOTIFICATION_ID
        )
        if (!success) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.RequestNotificationPermission)
            }
        }
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

            val today = LocalDate.now()
            for (i in 6 downTo 0) {
                val day = today.minusDays(i.toLong())
                val dayStart = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val daySteps = healthConnectManager.getStepsInRange(dayStart, dayEnd)
                progressionEngine.setDailySteps(day.toString(), daySteps)
            }

            _weeklySteps.value = progressionEngine.getWeeklySteps()

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

                _weeklySteps.value = progressionEngine.getWeeklySteps()

                questSyncUseCase.syncQuestsWithHealthData(buildDailyHealthSnapshot(startOfDay))

                fetchDailyHealthData()
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
        object RequestNotificationPermission : UiEvent()
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

    companion object {
        const val HYDRATION_GOAL_NOTIFICATION_ID = 1002
        const val HYDRATION_LOGGED_NOTIFICATION_ID = 1003
        const val TEST_NOTIFICATION_ID = 9999
    }
}
