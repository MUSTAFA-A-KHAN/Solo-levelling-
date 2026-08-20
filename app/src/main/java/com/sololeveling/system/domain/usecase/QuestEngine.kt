package com.sololeveling.system.domain.usecase

import com.sololeveling.system.domain.model.ActivityRequirement
import com.sololeveling.system.domain.model.ActivityType
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.model.QuestDifficulty
import com.sololeveling.system.domain.model.QuestType
import com.sololeveling.system.domain.repository.QuestRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class QuestEngine @Inject constructor(
    private val questRepository: QuestRepository,
    private val progressionEngine: ProgressionEngine
) {

    suspend fun generateDailyQuestsIfNeeded() {
        val todayStr = LocalDate.now().toString()
        val todayDailyQuests = questRepository.getDailyQuestsForDate(todayStr)

        if (todayDailyQuests.isEmpty()) {
            // Generate standard daily quests
            val dailyStepsQuest = Quest(
                id = "DAILY_STEPS_$todayStr",
                title = "Physical Conditioning",
                description = "Complete 5000 steps today to improve your basic fitness.",
                difficulty = QuestDifficulty.E,
                type = QuestType.DAILY,
                xpReward = 50,
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.STEPS,
                    targetValue = 5000.0
                )
            )

            val dailyWorkoutQuest = Quest(
                id = "DAILY_WORKOUT_$todayStr",
                title = "Strength Training",
                description = "Complete a 30-minute workout session.",
                difficulty = QuestDifficulty.D,
                type = QuestType.DAILY,
                xpReward = 100,
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.WORKOUT_DURATION_MINUTES,
                    targetValue = 30.0
                )
            )

            questRepository.addQuest(dailyStepsQuest)
            questRepository.addQuest(dailyWorkoutQuest)
        }
    }

    suspend fun evaluateQuests(
        player: Player,
        todaySteps: Double,
        todayWorkoutMinutes: Double
    ): Player {
        val activeQuests = questRepository.getActiveQuests().firstOrNull() ?: emptyList()
        var updatedPlayer = player

        for (quest in activeQuests) {
            val requirement = quest.requiredActivity ?: continue

            val currentValue = when (requirement.activityType) {
                ActivityType.STEPS -> todaySteps
                ActivityType.WORKOUT_DURATION_MINUTES -> todayWorkoutMinutes
                else -> requirement.currentValue // Custom quests handled elsewhere
            }

            val updatedRequirement = requirement.copy(currentValue = currentValue)
            val isCompleted = currentValue >= requirement.targetValue

            var updatedQuest = quest.copy(
                requiredActivity = updatedRequirement,
                isCompleted = isCompleted
            )

            if (isCompleted) {
                // Apply quest rewards to player
                updatedPlayer = progressionEngine.addXp(updatedPlayer, quest.xpReward)
                // TODO: Apply attribute rewards when implemented fully in MVP

                // Re-evaluate rank
                updatedPlayer = updatedPlayer.copy(rank = progressionEngine.evaluateRank(updatedPlayer))
            }

            questRepository.updateQuest(updatedQuest)
        }

        return updatedPlayer
    }
}
