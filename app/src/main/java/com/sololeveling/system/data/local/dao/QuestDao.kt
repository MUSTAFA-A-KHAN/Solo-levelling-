package com.sololeveling.system.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sololeveling.system.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quest_table WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quest_table WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quest_table WHERE id LIKE '%' || :dateSuffix || '%' AND type = 'DAILY'")
    suspend fun getDailyQuestsForDate(dateSuffix: String): List<QuestEntity>

    @Query("SELECT * FROM quest_table WHERE id = :id LIMIT 1")
    suspend fun getQuestById(id: String): QuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity)

    @Update
    suspend fun updateQuest(quest: QuestEntity)
}
