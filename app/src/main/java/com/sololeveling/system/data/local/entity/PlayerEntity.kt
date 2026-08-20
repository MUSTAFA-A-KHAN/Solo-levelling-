package com.sololeveling.system.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerAttributes
import com.sololeveling.system.domain.model.Rank

@Entity(tableName = "player_table")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val title: String?,
    val level: Int,
    val xp: Long,
    val nextLevelXp: Long,
    val rank: String,
    val strength: Double,
    val agility: Double,
    val vitality: Double,
    val intelligence: Double,
    val discipline: Double,
    val endurance: Double,
    val availableAttributePoints: Int
)

fun PlayerEntity.toDomain(): Player {
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
        availableAttributePoints = availableAttributePoints
    )
}

fun Player.toEntity(): PlayerEntity {
    return PlayerEntity(
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
        availableAttributePoints = availableAttributePoints
    )
}
