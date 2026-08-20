package com.sololeveling.system.data.repository

import com.google.gson.Gson
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

    private val gson = Gson()

    override fun getActiveQuests(): Flow<List<Quest>> {
        return questDao.getActiveQuests().map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    attributeRewards = gson.fromJson(entity.attributeRewardsJson, object : com.google.gson.reflect.TypeToken<Map<com.sololeveling.system.domain.model.AttributeType, Double>>() {}.type) ?: emptyMap(),
                    requiredActivity = gson.fromJson(entity.requiredActivityJson, com.sololeveling.system.domain.model.ActivityRequirement::class.java)
                )
            }
        }
    }

    override fun getCompletedQuests(): Flow<List<Quest>> {
        return questDao.getCompletedQuests().map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    attributeRewards = gson.fromJson(entity.attributeRewardsJson, object : com.google.gson.reflect.TypeToken<Map<com.sololeveling.system.domain.model.AttributeType, Double>>() {}.type) ?: emptyMap(),
                    requiredActivity = gson.fromJson(entity.requiredActivityJson, com.sololeveling.system.domain.model.ActivityRequirement::class.java)
                )
            }
        }
    }

    override suspend fun updateQuest(quest: Quest) {
        questDao.updateQuest(quest.toEntity(
            attributeRewardsJson = gson.toJson(quest.attributeRewards),
            requiredActivityJson = gson.toJson(quest.requiredActivity)
        ))
    }

    override suspend fun addQuest(quest: Quest) {
        questDao.insertQuest(quest.toEntity(
            attributeRewardsJson = gson.toJson(quest.attributeRewards),
            requiredActivityJson = gson.toJson(quest.requiredActivity)
        ))
    }
}
