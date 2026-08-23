package com.sololeveling.system.data.remote.firebase

import com.google.gson.Gson
import com.sololeveling.system.domain.model.ActivityRequirement
import com.sololeveling.system.domain.model.AttributeType
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.model.QuestDifficulty
import com.sololeveling.system.domain.model.QuestType

data class QuestDocument(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val difficulty: String = "E",
    val type: String = "DAILY",
    val isCompleted: Boolean = false,
    val xpReward: Long = 0,
    val attributeRewardsJson: String = "{}",
    val requiredActivityJson: String? = null
)

private val gson = Gson()

fun QuestDocument.toDomain(): Quest {
    val attributeRewards = gson.fromJson<Map<AttributeType, Double>>(
        attributeRewardsJson,
        object : com.google.gson.reflect.TypeToken<Map<AttributeType, Double>>() {}.type
    ) ?: emptyMap()
    val requiredActivity = requiredActivityJson?.let {
        gson.fromJson(it, ActivityRequirement::class.java)
    }
    return Quest(
        id = id,
        title = title,
        description = description,
        date = date,
        difficulty = QuestDifficulty.valueOf(difficulty),
        type = QuestType.valueOf(type),
        isCompleted = isCompleted,
        xpReward = xpReward,
        attributeRewards = attributeRewards,
        requiredActivity = requiredActivity
    )
}

fun Quest.toDocument(): QuestDocument {
    return QuestDocument(
        id = id,
        title = title,
        description = description,
        date = date,
        difficulty = difficulty.name,
        type = type.name,
        isCompleted = isCompleted,
        xpReward = xpReward,
        attributeRewardsJson = gson.toJson(attributeRewards),
        requiredActivityJson = gson.toJson(requiredActivity)
    )
}
