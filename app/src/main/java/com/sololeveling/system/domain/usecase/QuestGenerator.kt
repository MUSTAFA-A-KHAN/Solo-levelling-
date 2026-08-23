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
            questRepository.deleteQuestsByType(QuestType.SHORT)
            generateDailyQuest(dateString)
            generateShortQuests(dateString)
            preferences.setLastDailyQuestDate(dateString)
        }

        if (lastWeeklyDate != weekString) {
            generateWeeklyQuest(weekString)
            preferences.setLastWeeklyQuestDate(weekString)
        }
    }

    private suspend fun generateShortQuests(dateString: String) {
        val quests = listOf(
            Quest(
                id = "short_${dateString}_body_awakening",
                title = "Body Awakening",
                description = "Reach your personalized step target for today.",
                difficulty = QuestDifficulty.E,
                type = QuestType.SHORT,
                date = dateString,
                xpReward = 80,
                attributeRewards = mapOf(AttributeType.ENDURANCE to 0.3, AttributeType.VITALITY to 0.2),
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.STEPS,
                    targetValue = 8000.0,
                    currentValue = 0.0
                )
            ),
            Quest(
                id = "short_${dateString}_mana_recovery",
                title = "Mana Recovery",
                description = "Get your sleep target to restore your mana.",
                difficulty = QuestDifficulty.E,
                type = QuestType.SHORT,
                date = dateString,
                xpReward = 80,
                attributeRewards = mapOf(AttributeType.INTELLIGENCE to 0.3, AttributeType.VITALITY to 0.2),
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.SLEEP_MINUTES,
                    targetValue = 420.0,
                    currentValue = 0.0
                )
            ),
            Quest(
                id = "short_${dateString}_iron_body",
                title = "Iron Body",
                description = "Complete at least one exercise session.",
                difficulty = QuestDifficulty.D,
                type = QuestType.SHORT,
                date = dateString,
                xpReward = 100,
                attributeRewards = mapOf(AttributeType.STRENGTH to 0.4, AttributeType.ENDURANCE to 0.3),
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.EXERCISE_SESSIONS,
                    targetValue = 1.0,
                    currentValue = 0.0
                )
            ),
            Quest(
                id = "short_${dateString}_the_long_march",
                title = "The Long March",
                description = "Walk continuously for a sustained duration.",
                difficulty = QuestDifficulty.D,
                type = QuestType.SHORT,
                date = dateString,
                xpReward = 100,
                attributeRewards = mapOf(AttributeType.ENDURANCE to 0.4, AttributeType.AGILITY to 0.2),
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.WORKOUT_DURATION_MINUTES,
                    targetValue = 30.0,
                    currentValue = 0.0
                )
            ),
            Quest(
                id = "short_${dateString}_no_slacking",
                title = "No Slacking",
                description = "Reach your full activity target before the day ends.",
                difficulty = QuestDifficulty.C,
                type = QuestType.SHORT,
                date = dateString,
                xpReward = 120,
                attributeRewards = mapOf(AttributeType.DISCIPLINE to 0.4, AttributeType.ENDURANCE to 0.3),
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.STEPS,
                    targetValue = 10000.0,
                    currentValue = 0.0
                )
            ),
            Quest(
                id = "short_${dateString}_first_movement",
                title = "First Movement",
                description = "Record activity before 10:00 AM.",
                difficulty = QuestDifficulty.E,
                type = QuestType.SHORT,
                date = dateString,
                xpReward = 60,
                attributeRewards = mapOf(AttributeType.AGILITY to 0.3, AttributeType.DISCIPLINE to 0.2),
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.FIRST_MOVEMENT,
                    targetValue = 1.0,
                    currentValue = 0.0
                )
            ),
            Quest(
                id = "short_${dateString}_final_push",
                title = "Final Push",
                description = "Complete your remaining activity target in the evening.",
                difficulty = QuestDifficulty.C,
                type = QuestType.SHORT,
                date = dateString,
                xpReward = 120,
                attributeRewards = mapOf(AttributeType.DISCIPLINE to 0.4, AttributeType.STRENGTH to 0.3),
                requiredActivity = ActivityRequirement(
                    activityType = ActivityType.EVENING_STEPS,
                    targetValue = 2000.0,
                    currentValue = 0.0
                )
            )
        )

        quests.forEach { questRepository.addQuest(it) }
    }

    private suspend fun generateDailyQuest(dateString: String) {
        val dailyQuest = Quest(
            id = "daily_${dateString}_1",
            title = "The Daily Prep",
            description = "Complete your daily routine to stay in shape.",
            difficulty = QuestDifficulty.E,
            type = QuestType.DAILY,
            date = dateString,
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
            date = dateString,
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
                date = LocalDate.now().toString(),
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
