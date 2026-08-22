package com.sololeveling.system.domain.usecase

import com.sololeveling.system.data.local.SystemPreferences
import com.sololeveling.system.domain.model.*
import com.sololeveling.system.domain.repository.QuestRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class QuestGenerator @Inject constructor(
    private val questRepository: QuestRepository,
    private val preferences: SystemPreferences
) {

    suspend fun checkAndGenerateQuests() {
        val currentDate = LocalDate.now()
        val currentWeek = currentDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
        val currentYear = currentDate.year

        val dateString = currentDate.toString()
        val weekString = "${currentYear}-W${currentWeek}"

        val lastDailyDate = preferences.lastDailyQuestDate.firstOrNull()
        val lastWeeklyDate = preferences.lastWeeklyQuestDate.firstOrNull()

        if (lastDailyDate != dateString) {
            generateDailyQuest(dateString)
            preferences.setLastDailyQuestDate(dateString)
        }

        if (lastWeeklyDate != weekString) {
            generateWeeklyQuest(weekString)
            preferences.setLastWeeklyQuestDate(weekString)
        }
    }

    private suspend fun generateDailyQuest(dateString: String) {
        val dailyQuest = Quest(
            id = "daily_${dateString}_1",
            title = "The Daily Prep",
            description = "Complete your daily routine to stay in shape.",
            difficulty = QuestDifficulty.E,
            type = QuestType.DAILY,
            isCompleted = false,
            xpReward = 100,
            attributeRewards = mapOf(
                AttributeType.STRENGTH to 0.5,
                AttributeType.AGILITY to 0.5,
                AttributeType.ENDURANCE to 0.5
            ),
            requiredActivity = ActivityRequirement(
                activityType = ActivityType.STEPS,
                targetValue = 10000.0,
                currentValue = 0.0
            )
        )

        val dailyQuest2 = Quest(
            id = "daily_${dateString}_2",
            title = "Physical Conditioning",
            description = "Push your limits.",
            difficulty = QuestDifficulty.E,
            type = QuestType.DAILY,
            isCompleted = false,
            xpReward = 150,
            attributeRewards = mapOf(
                AttributeType.STRENGTH to 1.0,
                AttributeType.VITALITY to 1.0
            ),
            requiredActivity = ActivityRequirement(
                activityType = ActivityType.WORKOUT_DURATION_MINUTES,
                targetValue = 30.0,
                currentValue = 0.0
            )
        )

        questRepository.addQuest(dailyQuest)
        questRepository.addQuest(dailyQuest2)
    }

    private suspend fun generateWeeklyQuest(weekString: String) {
        val weeklyQuest = Quest(
            id = "weekly_${weekString}",
            title = "Weekly Milestone: The Long Run",
            description = "Consistent effort yields great results.",
            difficulty = QuestDifficulty.C,
            type = QuestType.WEEKLY,
            isCompleted = false,
            xpReward = 1000,
            attributeRewards = mapOf(
                AttributeType.ENDURANCE to 5.0,
                AttributeType.DISCIPLINE to 5.0
            ),
            requiredActivity = ActivityRequirement(
                activityType = ActivityType.STEPS,
                targetValue = 50000.0,
                currentValue = 0.0
            )
        )

        questRepository.addQuest(weeklyQuest)
    }
}
