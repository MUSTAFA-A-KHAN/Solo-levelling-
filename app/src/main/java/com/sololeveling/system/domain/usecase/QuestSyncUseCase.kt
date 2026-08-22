package com.sololeveling.system.domain.usecase

import com.sololeveling.system.domain.model.*
import com.sololeveling.system.domain.repository.PlayerRepository
import com.sololeveling.system.domain.repository.QuestRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class QuestSyncUseCase @Inject constructor(
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val progressionEngine: ProgressionEngine
) {

    suspend fun syncQuestsWithHealthData(steps: Long, workoutMinutes: Long) {
        val activeQuests = questRepository.getActiveQuests().firstOrNull() ?: return
        var player = playerRepository.getPlayer().firstOrNull() ?: return

        activeQuests.forEach { quest ->
            val req = quest.requiredActivity
            if (req != null) {
                var newCurrentValue = req.currentValue

                when (req.activityType) {
                    ActivityType.STEPS -> newCurrentValue += steps
                    ActivityType.WORKOUT_DURATION_MINUTES -> newCurrentValue += workoutMinutes
                    else -> {} // Manual sync types ignored here
                }

                if (newCurrentValue > req.currentValue) {
                    val isCompleted = newCurrentValue >= req.targetValue
                    val updatedQuest = quest.copy(
                        requiredActivity = req.copy(currentValue = newCurrentValue),
                        isCompleted = isCompleted
                    )

                    questRepository.updateQuest(updatedQuest)

                    if (isCompleted) {
                        player = applyQuestRewards(player, updatedQuest)
                    }
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

        val updatedQuest = quest.copy(
            requiredActivity = req.copy(currentValue = newCurrentValue),
            isCompleted = isCompleted
        )

        questRepository.updateQuest(updatedQuest)

        if (isCompleted) {
            player = applyQuestRewards(player, updatedQuest)
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
