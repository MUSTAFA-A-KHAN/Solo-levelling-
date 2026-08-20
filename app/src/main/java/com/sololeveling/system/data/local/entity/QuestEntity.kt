package com.sololeveling.system.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sololeveling.system.domain.model.*

@Entity(tableName = "quest_table")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val type: String,
    val isCompleted: Boolean,
    val xpReward: Long,
    val attributeRewardsJson: String,
    val requiredActivityJson: String?
)

fun QuestEntity.toDomain(
    attributeRewards: Map<AttributeType, Double>,
    requiredActivity: ActivityRequirement?
): Quest {
    return Quest(
        id = id,
        title = title,
        description = description,
        difficulty = QuestDifficulty.valueOf(difficulty),
        type = QuestType.valueOf(type),
        isCompleted = isCompleted,
        xpReward = xpReward,
        attributeRewards = attributeRewards,
        requiredActivity = requiredActivity
    )
}

fun Quest.toEntity(
    attributeRewardsJson: String,
    requiredActivityJson: String?
): QuestEntity {
    return QuestEntity(
        id = id,
        title = title,
        description = description,
        difficulty = difficulty.name,
        type = type.name,
        isCompleted = isCompleted,
        xpReward = xpReward,
        attributeRewardsJson = attributeRewardsJson,
        requiredActivityJson = requiredActivityJson
    )
}
