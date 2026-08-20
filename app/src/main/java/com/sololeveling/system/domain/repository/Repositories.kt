package com.sololeveling.system.domain.repository

import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.model.SystemEvent
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayer(): Flow<Player?>
    suspend fun updatePlayer(player: Player)
    suspend fun initializePlayer(name: String)
}

interface QuestRepository {
    fun getActiveQuests(): Flow<List<Quest>>
    fun getCompletedQuests(): Flow<List<Quest>>
    suspend fun getDailyQuestsForDate(dateSuffix: String): List<Quest>
    suspend fun updateQuest(quest: Quest)
    suspend fun addQuest(quest: Quest)
}

interface SystemEventRepository {
    fun getRecentEvents(): Flow<List<SystemEvent>>
    fun getUnreadEventCount(): Flow<Int>
    suspend fun addEvent(event: SystemEvent)
    suspend fun markAllAsRead()
}
