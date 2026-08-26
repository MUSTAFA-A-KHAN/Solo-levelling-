package com.sololeveling.system.domain.usecase

import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.Rank

class ProgressionEngine {

    // Deterministic XP calculation for leveling
    fun calculateNextLevelXp(level: Int): Long {
        // Curve: Base 100, increases non-linearly
        return (100 * Math.pow(1.2, (level - 1).toDouble())).toLong()
    }

    fun addXp(player: Player, xpGained: Long): Player {
        var newXp = player.xp + xpGained
        var newLevel = player.level
        var newNextLevelXp = player.nextLevelXp
        var newAvailableAttributePoints = player.availableAttributePoints

        while (newXp >= newNextLevelXp) {
            newXp -= newNextLevelXp
            newLevel++
            newAvailableAttributePoints += 3 // 3 points per level
            newNextLevelXp = calculateNextLevelXp(newLevel)
        }

        return player.copy(
            level = newLevel,
            xp = newXp,
            nextLevelXp = newNextLevelXp,
            availableAttributePoints = newAvailableAttributePoints
        )
    }

    fun evaluateRank(player: Player): Rank {
        // Deterministic rank evaluation based on level and total attributes
        val totalAttributes = with(player.attributes) {
            strength + agility + vitality + intelligence + discipline + endurance
        }

        return when {
            player.level >= 100 && totalAttributes >= 2000 -> Rank.S
            player.level >= 80 && totalAttributes >= 1200 -> Rank.A
            player.level >= 60 && totalAttributes >= 700 -> Rank.B
            player.level >= 40 && totalAttributes >= 400 -> Rank.C
            player.level >= 20 && totalAttributes >= 200 -> Rank.D
            else -> Rank.E
        }
    }

    /**
     * Converts real-world health data deterministically into System progression.
     * 100 steps = 1 XP, +0.01 Agility/Endurance
     * 1 min workout = 10 XP, +0.1 Strength/Vitality
     */
    fun processHealthData(player: Player, steps: Long, workoutMinutes: Long, syncTime: Long): Player {
        if (steps <= 0 && workoutMinutes <= 0) return player.copy(lastSyncTime = syncTime)

        val xpFromSteps = steps / 100
        val xpFromWorkouts = workoutMinutes * 10
        val totalXpGained = xpFromSteps + xpFromWorkouts

        val agilityGain = (steps / 100) * 0.01
        val enduranceGain = (steps / 100) * 0.01
        val strengthGain = workoutMinutes * 0.1
        val vitalityGain = workoutMinutes * 0.1

        val newAttributes = player.attributes.copy(
            agility = player.attributes.agility + agilityGain,
            endurance = player.attributes.endurance + enduranceGain,
            strength = player.attributes.strength + strengthGain,
            vitality = player.attributes.vitality + vitalityGain
        )

        val playerWithXp = addXp(player, totalXpGained)
        val finalPlayer = playerWithXp.copy(
            attributes = newAttributes,
            footsteps = player.footsteps + steps,
            lastSyncTime = syncTime
        )

        return finalPlayer.copy(rank = evaluateRank(finalPlayer))
    }
}
