package com.sololeveling.system.domain.model

data class SystemEvent(
    val id: String,
    val type: EventType,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class EventType {
    LEVEL_UP,
    RANK_UP,
    QUEST_COMPLETED,
    QUEST_FAILED,
    ACHIEVEMENT_UNLOCKED,
    SYSTEM_NOTIFICATION
}
