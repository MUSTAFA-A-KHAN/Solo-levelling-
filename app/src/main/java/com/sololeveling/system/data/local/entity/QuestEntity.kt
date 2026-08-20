package com.sololeveling.system.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sololeveling.system.domain.model.ActivityRequirement
import com.sololeveling.system.domain.model.ActivityType
import com.sololeveling.system.domain.model.AttributeType
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.model.QuestDifficulty
import com.sololeveling.system.domain.model.QuestType

@Entity(tableName = "quest_table")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val type: String,
    val isCompleted: Boolean,
    val xpReward: Long,
    val requiredActivityType: String?,
    val requiredTargetValue: Double?,
    val requiredCurrentValue: Double?,
    val createdAt: Long = System.currentTimeMillis()
)

fun QuestEntity.toDomain(): Quest {
    val requirement = if (requiredActivityType != null && requiredTargetValue != null) {
        ActivityRequirement(
            activityType = ActivityType.valueOf(requiredActivityType),
            targetValue = requiredTargetValue,
            currentValue = requiredCurrentValue ?: 0.0
        )
    } else null

    return Quest(
        id = id,
        title = title,
        description = description,
        difficulty = QuestDifficulty.valueOf(difficulty),
        type = QuestType.valueOf(type),
        isCompleted = isCompleted,
        xpReward = xpReward,
        attributeRewards = emptyMap(), // Keep simple for MVP
        requiredActivity = requirement
    )
}

fun Quest.toEntity(): QuestEntity {
    return QuestEntity(
        id = id,
        title = title,
        description = description,
        difficulty = difficulty.name,
        type = type.name,
        isCompleted = isCompleted,
        xpReward = xpReward,
        requiredActivityType = requiredActivity?.activityType?.name,
        requiredTargetValue = requiredActivity?.targetValue,
        requiredCurrentValue = requiredActivity?.currentValue
    )
}
