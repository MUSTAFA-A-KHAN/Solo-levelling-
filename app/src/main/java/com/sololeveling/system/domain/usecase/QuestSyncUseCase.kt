package com.sololeveling.system.domain.usecase

import com.sololeveling.system.domain.model.*
import com.sololeveling.system.domain.repository.PlayerRepository
import com.sololeveling.system.domain.repository.QuestRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class QuestSyncUseCase @Inject constructor(
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val progressionEngine: ProgressionEngine
) {

    companion object {
        private const val FIRST_MOVEMENT_CUTOFF_HOUR = 10
        private const val EVENING_START_HOUR = 18
    }

    suspend fun syncQuestsWithHealthData(snapshot: HealthSnapshot) {
        val activeQuests = questRepository.getActiveQuests().firstOrNull() ?: return
        var player = playerRepository.getPlayer().firstOrNull() ?: return

        val firstMovementCutoff = LocalDate.now()
            .atTime(FIRST_MOVEMENT_CUTOFF_HOUR, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val eveningStart = LocalDate.now()
            .atTime(EVENING_START_HOUR, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        activeQuests.forEach { quest ->
            val req = quest.requiredActivity ?: return@forEach

            val newCurrentValue = when (req.activityType) {
                ActivityType.STEPS -> snapshot.steps.toDouble()
                ActivityType.WORKOUT_DURATION_MINUTES -> snapshot.workoutMinutes.toDouble()
                ActivityType.RUNNING_DISTANCE_METERS -> 0.0
                ActivityType.STUDY_MINUTES -> 0.0
                ActivityType.SLEEP_MINUTES -> snapshot.sleepMinutes.toDouble()
                ActivityType.EXERCISE_SESSIONS -> snapshot.exerciseSessions.toDouble()
                ActivityType.EVENING_STEPS -> snapshot.eveningSteps.toDouble()
                ActivityType.FIRST_MOVEMENT -> {
                    if (snapshot.firstActivityTime != null &&
                        snapshot.firstActivityTime <= firstMovementCutoff
                    ) {
                        1.0
                    } else {
                        0.0
                    }
                }
            }

            val isCompleted = newCurrentValue >= req.targetValue
            val wasCompleted = quest.isCompleted

            if (newCurrentValue != req.currentValue || isCompleted != wasCompleted) {
                questRepository.updateQuest(
                    quest.copy(
                        requiredActivity = req.copy(currentValue = newCurrentValue),
                        isCompleted = isCompleted
                    )
                )

                if (isCompleted && !wasCompleted) {
                    player = applyQuestRewards(player, quest)
                }
            }
        }

        playerRepository.updatePlayer(player)
    }

    suspend fun manualSyncQuest(questId: String, addedValue: Double) {
        val activeQuests = questRepository.getActiveQuests().firstOrNull() ?: return
        val quest = activeQuests.find { it.id == questId } ?: return
        val req = quest.requiredActivity ?: return
        var player = playerRepository.getPlayer().firstOrNull() ?: return

        val newCurrentValue = req.currentValue + addedValue
        val isCompleted = newCurrentValue >= req.targetValue
        val wasCompleted = quest.isCompleted

        questRepository.updateQuest(
            quest.copy(
                requiredActivity = req.copy(currentValue = newCurrentValue),
                isCompleted = isCompleted
            )
        )

        if (isCompleted && !wasCompleted) {
            player = applyQuestRewards(player, quest)
            playerRepository.updatePlayer(player)
        }
    }

    private fun applyQuestRewards(player: Player, quest: Quest): Player {
        val newAttributes = player.attributes.copy(
            strength = player.attributes.strength + (quest.attributeRewards[AttributeType.STRENGTH] ?: 0.0),
            agility = player.attributes.agility + (quest.attributeRewards[AttributeType.AGILITY] ?: 0.0),
            vitality = player.attributes.vitality + (quest.attributeRewards[AttributeType.VITALITY] ?: 0.0),
            intelligence = player.attributes.intelligence + (quest.attributeRewards[AttributeType.INTELLIGENCE] ?: 0.0),
            discipline = player.attributes.discipline + (quest.attributeRewards[AttributeType.DISCIPLINE] ?: 0.0),
            endurance = player.attributes.endurance + (quest.attributeRewards[AttributeType.ENDURANCE] ?: 0.0)
        )

        val playerWithAttributes = player.copy(attributes = newAttributes)
        val playerWithXp = progressionEngine.addXp(playerWithAttributes, quest.xpReward)

        return playerWithXp.copy(rank = progressionEngine.evaluateRank(playerWithXp))
    }
}
