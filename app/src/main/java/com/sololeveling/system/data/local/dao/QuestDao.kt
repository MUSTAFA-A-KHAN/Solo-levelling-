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
    @Query("SELECT * FROM quest_table WHERE isCompleted = 0")
    fun getActiveQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quest_table WHERE isCompleted = 1")
    fun getCompletedQuests(): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("SELECT * FROM quest_table WHERE type = :type")
    suspend fun getQuestsByType(type: String): List<QuestEntity>

    @Query("DELETE FROM quest_table WHERE type = :type")
    suspend fun deleteQuestsByType(type: String)
}
