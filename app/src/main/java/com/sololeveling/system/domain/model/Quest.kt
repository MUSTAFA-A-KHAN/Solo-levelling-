package com.sololeveling.system.domain.model

enum class AttributeType {
    STRENGTH, AGILITY, VITALITY, INTELLIGENCE, DISCIPLINE, ENDURANCE
}

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: QuestDifficulty = QuestDifficulty.E,
    val type: QuestType = QuestType.DAILY,
    val isCompleted: Boolean = false,
    val xpReward: Long = 0,
    val attributeRewards: Map<AttributeType, Double> = emptyMap(),
    val requiredActivity: ActivityRequirement? = null
)

enum class QuestDifficulty(val value: Int) { E(1), D(2), C(3), B(4), A(5), S(6) }
enum class QuestType { DAILY, WEEKLY, MAIN, SIDE, HIDDEN }

data class ActivityRequirement(
    val activityType: ActivityType,
    val targetValue: Double,
    val currentValue: Double = 0.0
)

enum class ActivityType {
    STEPS,
    WORKOUT_DURATION_MINUTES,
    RUNNING_DISTANCE_METERS,
    STUDY_MINUTES
}
