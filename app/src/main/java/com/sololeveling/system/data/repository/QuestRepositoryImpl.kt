package com.sololeveling.system.data.repository

import com.sololeveling.system.data.local.dao.QuestDao
import com.sololeveling.system.data.local.entity.toDomain
import com.sololeveling.system.data.local.entity.toEntity
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao
) : QuestRepository {

    override fun getActiveQuests(): Flow<List<Quest>> {
        return questDao.getActiveQuests().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCompletedQuests(): Flow<List<Quest>> {
        return questDao.getCompletedQuests().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDailyQuestsForDate(dateSuffix: String): List<Quest> {
        return questDao.getDailyQuestsForDate(dateSuffix).map { it.toDomain() }
    }

    override suspend fun updateQuest(quest: Quest) {
        questDao.updateQuest(quest.toEntity())
    }

    override suspend fun addQuest(quest: Quest) {
        questDao.insertQuest(quest.toEntity())
    }
}
