package com.sololeveling.system.domain.usecase

import api.Api
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerAttributes
import com.sololeveling.system.domain.model.Rank

class ProgressionEngine {

    // Deterministic XP calculation for leveling
    fun calculateNextLevelXp(level: Int): Long {
        return Api.calculateNextLevelXp(level)
    }

    fun addXp(player: Player, xpGained: Long): Player {
        val result = Api.addXp(
            player.level,
            player.xp,
            player.nextLevelXp,
            player.availableAttributePoints,
            player.rank.value,
            player.rank.title,
            player.footsteps,
            player.lastSyncTime,
            player.attributes.strength,
            player.attributes.agility,
            player.attributes.vitality,
            player.attributes.intelligence,
            player.attributes.discipline,
            player.attributes.endurance,
            xpGained
        )

        return player.copy(
            level = result.level,
            xp = result.xp,
            nextLevelXp = result.nextLevelXp,
            availableAttributePoints = result.availableAttributePoints,
            rank = Rank.values().find { it.value == result.rankValue } ?: Rank.E,
            footsteps = result.footsteps,
            lastSyncTime = result.lastSyncTime,
            attributes = PlayerAttributes(
                strength = result.strength,
                agility = result.agility,
                vitality = result.vitality,
                intelligence = result.intelligence,
                discipline = result.discipline,
                endurance = result.endurance
            )
        )
    }

    /**
     * Stores the absolute step total for a single calendar day in the Go core's
     * per-day store. Idempotent — call it whenever fresh daily data is available.
     */
    fun setDailySteps(date: String, steps: Long) {
        Api.setDailySteps(date, steps)
    }

    /**
     * Returns the footsteps counted for the ISO week (Mon–Sun) containing today,
     * computed by the Go core from its per-day step store.
     */
    fun getWeeklySteps(): Long {
        val today = java.time.LocalDate.now().toString() // "YYYY-MM-DD"
        return Api.weeklySteps(today)
    }

    fun evaluateRank(player: Player): Rank {
        val result = Api.evaluateRank(
            player.level,
            player.xp,
            player.nextLevelXp,
            player.availableAttributePoints,
            player.rank.value,
            player.rank.title,
            player.footsteps,
            player.lastSyncTime,
            player.attributes.strength,
            player.attributes.agility,
            player.attributes.vitality,
            player.attributes.intelligence,
            player.attributes.discipline,
            player.attributes.endurance
        )
        return Rank.values().find { it.value == result.rankValue } ?: Rank.E
    }

    /**
     * Converts real-world health data deterministically into System progression.
     * 100 steps = 1 XP, +0.01 Agility/Endurance
     * 1 min workout = 10 XP, +0.1 Strength/Vitality
     */
    fun processHealthData(player: Player, steps: Long, workoutMinutes: Long, syncTime: Long): Player {
        if (steps <= 0 && workoutMinutes <= 0) return player.copy(lastSyncTime = syncTime)

        val result = Api.processHealthData(
            player.level,
            player.xp,
            player.nextLevelXp,
            player.availableAttributePoints,
            player.rank.value,
            player.rank.title,
            player.footsteps,
            player.lastSyncTime,
            player.attributes.strength,
            player.attributes.agility,
            player.attributes.vitality,
            player.attributes.intelligence,
            player.attributes.discipline,
            player.attributes.endurance,
            steps,
            workoutMinutes,
            syncTime
        )

        return player.copy(
            level = result.level,
            xp = result.xp,
            nextLevelXp = result.nextLevelXp,
            availableAttributePoints = result.availableAttributePoints,
            rank = Rank.values().find { it.value == result.rankValue } ?: Rank.E,
            footsteps = result.footsteps,
            lastSyncTime = result.lastSyncTime,
            attributes = PlayerAttributes(
                strength = result.strength,
                agility = result.agility,
                vitality = result.vitality,
                intelligence = result.intelligence,
                discipline = result.discipline,
                endurance = result.endurance
            )
        )
    }
}
