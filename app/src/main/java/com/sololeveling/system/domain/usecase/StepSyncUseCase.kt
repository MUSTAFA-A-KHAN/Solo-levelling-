package com.sololeveling.system.domain.usecase

import android.util.Log
import com.sololeveling.system.data.health.HealthConnectManager
import com.sololeveling.system.domain.model.HealthSnapshot
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.LeaderboardRepository
import com.sololeveling.system.domain.repository.PlayerRepository
import com.sololeveling.system.domain.repository.QuestRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepSyncUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val questRepository: QuestRepository,
    private val progressionEngine: ProgressionEngine,
    private val questSyncUseCase: QuestSyncUseCase,
    private val healthConnectManager: HealthConnectManager,
    private val leaderboardRepository: LeaderboardRepository,
    private val authRepository: AuthRepository
) {
    private val syncMutex = Mutex()

    /**
     * Executes automatic step synchronization.
     * Guaranteed thread-safe and non-duplicate using Mutex lock.
     */
    suspend fun executeSync(): Boolean = syncMutex.withLock {
        try {
            val currentPlayer = playerRepository.getPlayer().firstOrNull() ?: run {
                Log.d(TAG, "Sync skipped: Player not initialized")
                return false
            }

            if (!healthConnectManager.isHealthConnectAvailable() || !healthConnectManager.hasAllPermissions()) {
                Log.d(TAG, "Sync skipped: Health Connect unavailable or missing permissions")
                return false
            }

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val since = if (currentPlayer.xp == 0L && currentPlayer.lastSyncTime > startOfDay) {
                startOfDay
            } else {
                currentPlayer.lastSyncTime
            }

            val now = System.currentTimeMillis()

            val steps = healthConnectManager.getRecentSteps(since)
            val workoutMinutes = healthConnectManager.getRecentWorkoutDurationMinutes(since)

            // Seed per-day steps for weekly computation
            val today = LocalDate.now()
            for (i in 6 downTo 0) {
                val day = today.minusDays(i.toLong())
                val dayStart = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val daySteps = healthConnectManager.getStepsInRange(dayStart, dayEnd)
                progressionEngine.setDailySteps(day.toString(), daySteps)
            }

            val updatedPlayer = progressionEngine.processHealthData(currentPlayer, steps, workoutMinutes, now)

            // Avoid unnecessary Firestore writes: check if meaningful player stats changed (excluding lastSyncTime timestamp)
            val statsChanged = updatedPlayer.copy(lastSyncTime = 0) != currentPlayer.copy(lastSyncTime = 0)
            val timeUpdated = updatedPlayer.lastSyncTime != currentPlayer.lastSyncTime

            if (statsChanged || timeUpdated) {
                // Save locally (updatePlayer also pushes to Firestore if authenticated)
                playerRepository.updatePlayer(updatedPlayer)

                val snapshot = buildDailyHealthSnapshot(startOfDay)
                questSyncUseCase.syncQuestsWithHealthData(snapshot)

                if (statsChanged && authRepository.getCurrentUser() != null) {
                    val completedQuests = questRepository.getCompletedQuests().firstOrNull()?.size ?: 0
                    try {
                        leaderboardRepository.updateMyEntry(updatedPlayer, completedQuests)
                    } catch (e: Exception) {
                        Log.e(TAG, "Leaderboard update failed (offline mode): ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Step sync complete. Stats changed: $statsChanged")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error executing step sync", e)
            return false
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

    companion object {
        private const val TAG = "StepSyncUseCase"
    }
}
