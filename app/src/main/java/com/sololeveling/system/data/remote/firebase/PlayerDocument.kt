package com.sololeveling.system.data.remote.firebase

import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerAttributes
import com.sololeveling.system.domain.model.Rank

data class PlayerDocument(
    val id: String = "currentPlayer",
    val name: String = "",
    val title: String? = null,
    val level: Int = 1,
    val xp: Long = 0,
    val nextLevelXp: Long = 100,
    val rank: String = "E",
    val strength: Double = 10.0,
    val agility: Double = 10.0,
    val vitality: Double = 10.0,
    val intelligence: Double = 10.0,
    val discipline: Double = 10.0,
    val endurance: Double = 10.0,
    val availableAttributePoints: Int = 0,
    val footsteps: Long = 0L,
    val lastSyncTime: Long = 0L
)

fun PlayerDocument.toDomain(): Player {
    return Player(
        id = id,
        name = name,
        title = title,
        level = level,
        xp = xp,
        nextLevelXp = nextLevelXp,
        rank = Rank.valueOf(rank),
        attributes = PlayerAttributes(
            strength = strength,
            agility = agility,
            vitality = vitality,
            intelligence = intelligence,
            discipline = discipline,
            endurance = endurance
        ),
        availableAttributePoints = availableAttributePoints,
        footsteps = footsteps,
        lastSyncTime = lastSyncTime
    )
}

fun Player.toDocument(): PlayerDocument {
    return PlayerDocument(
        id = id,
        name = name,
        title = title,
        level = level,
        xp = xp,
        nextLevelXp = nextLevelXp,
        rank = rank.name,
        strength = attributes.strength,
        agility = attributes.agility,
        vitality = attributes.vitality,
        intelligence = attributes.intelligence,
        discipline = attributes.discipline,
        endurance = attributes.endurance,
        availableAttributePoints = availableAttributePoints,
        footsteps = footsteps,
        lastSyncTime = lastSyncTime
    )
}
