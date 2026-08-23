package com.sololeveling.system.data.repository

import com.google.gson.Gson
import com.sololeveling.system.data.local.dao.QuestDao
import com.sololeveling.system.data.local.entity.toDomain
import com.sololeveling.system.data.local.entity.toEntity
import com.sololeveling.system.data.remote.firebase.FirestoreQuestDataSource
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.model.QuestType
import com.sololeveling.system.domain.model.AttributeType
import com.sololeveling.system.domain.model.ActivityRequirement
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao,
    private val firestoreQuestDataSource: FirestoreQuestDataSource,
    private val authRepository: AuthRepository
) : QuestRepository {

    private val gson = Gson()

    override fun getActiveQuests(): Flow<List<Quest>> {
        return questDao.getActiveQuests().map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    attributeRewards = gson.fromJson(entity.attributeRewardsJson, object : com.google.gson.reflect.TypeToken<Map<AttributeType, Double>>() {}.type) ?: emptyMap(),
                    requiredActivity = gson.fromJson(entity.requiredActivityJson, ActivityRequirement::class.java)
                )
            }
        }
    }

    override fun getCompletedQuests(): Flow<List<Quest>> {
        return questDao.getCompletedQuests().map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    attributeRewards = gson.fromJson(entity.attributeRewardsJson, object : com.google.gson.reflect.TypeToken<Map<AttributeType, Double>>() {}.type) ?: emptyMap(),
                    requiredActivity = gson.fromJson(entity.requiredActivityJson, ActivityRequirement::class.java)
                )
            }
        }
    }

    override fun getActiveQuestsForDate(date: String, weekString: String): Flow<List<Quest>> {
        return questDao.getActiveQuestsForDate(date, weekString).map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    attributeRewards = gson.fromJson(entity.attributeRewardsJson, object : com.google.gson.reflect.TypeToken<Map<AttributeType, Double>>() {}.type) ?: emptyMap(),
                    requiredActivity = gson.fromJson(entity.requiredActivityJson, ActivityRequirement::class.java)
                )
            }
        }
    }

    override fun getCompletedQuestsForDate(date: String, weekString: String): Flow<List<Quest>> {
        return questDao.getCompletedQuestsForDate(date, weekString).map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    attributeRewards = gson.fromJson(entity.attributeRewardsJson, object : com.google.gson.reflect.TypeToken<Map<AttributeType, Double>>() {}.type) ?: emptyMap(),
                    requiredActivity = gson.fromJson(entity.requiredActivityJson, ActivityRequirement::class.java)
                )
            }
        }
    }

    override fun getAllQuests(): Flow<List<Quest>> {
        return questDao.getAllQuests().map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    attributeRewards = gson.fromJson(entity.attributeRewardsJson, object : com.google.gson.reflect.TypeToken<Map<AttributeType, Double>>() {}.type) ?: emptyMap(),
                    requiredActivity = gson.fromJson(entity.requiredActivityJson, ActivityRequirement::class.java)
                )
            }
        }
    }

    override suspend fun updateQuest(quest: Quest) {
        questDao.updateQuest(toEntity(quest))
        authRepository.getCurrentUser()?.uid?.let { uid ->
            firestoreQuestDataSource.saveQuest(uid, quest)
        }
    }

    override suspend fun addQuest(quest: Quest) {
        questDao.insertQuest(toEntity(quest))
        authRepository.getCurrentUser()?.uid?.let { uid ->
            firestoreQuestDataSource.saveQuest(uid, quest)
        }
    }

    override suspend fun deleteQuestsByType(type: QuestType) {
        val entities = questDao.getQuestsByType(type.name)
        authRepository.getCurrentUser()?.uid?.let { uid ->
            entities.forEach { entity ->
                firestoreQuestDataSource.deleteQuest(uid, entity.id)
            }
        }
        questDao.deleteQuestsByType(type.name)
    }

    override suspend fun syncWithFirestore(uid: String) {
        val localQuests = getAllLocalQuests()
        val remoteQuests = firestoreQuestDataSource.getQuests(uid)
        val merged = mergeQuests(localQuests, remoteQuests)
        val fixed = merged.map { quest ->
            if (quest.date.isBlank()) quest.copy(date = inferDateFromId(quest.id) ?: "") else quest
        }

        fixed.forEach { questDao.insertQuest(toEntity(it)) }
        firestoreQuestDataSource.saveQuests(uid, fixed)
    }

    private fun inferDateFromId(id: String): String? {
        return when {
            id.startsWith("daily_") || id.startsWith("short_") -> {
                val parts = id.split("_")
                if (parts.size >= 3) parts[2] else null
            }
            id.startsWith("weekly_") -> {
                val parts = id.split("_")
                if (parts.size >= 2) parts[1] else null
            }
            else -> null
        }
    }

    private fun toEntity(quest: Quest) = quest.toEntity(
        attributeRewardsJson = gson.toJson(quest.attributeRewards),
        requiredActivityJson = gson.toJson(quest.requiredActivity)
    )

    private suspend fun getAllLocalQuests(): List<Quest> {
        val active = getActiveQuests().firstOrNull() ?: emptyList()
        val completed = getCompletedQuests().firstOrNull() ?: emptyList()
        return active + completed
    }

    private fun mergeQuests(local: List<Quest>, remote: List<Quest>): List<Quest> {
        val remoteMap = remote.associateBy { it.id }
        val result = mutableListOf<Quest>()
        local.forEach { localQuest ->
            result.add(remoteMap[localQuest.id] ?: localQuest)
        }
        remote.forEach { remoteQuest ->
            if (local.none { it.id == remoteQuest.id }) {
                result.add(remoteQuest)
            }
        }
        return result
    }
}
