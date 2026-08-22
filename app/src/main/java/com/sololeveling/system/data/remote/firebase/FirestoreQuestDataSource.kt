package com.sololeveling.system.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.sololeveling.system.domain.model.Quest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreQuestDataSource @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getQuests(uid: String): List<Quest> {
        val snapshot = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(QUESTS_SUBCOLLECTION)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(QuestDocument::class.java)?.toDomain()
        }
    }

    suspend fun saveQuests(uid: String, quests: List<Quest>) {
        val collection = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(QUESTS_SUBCOLLECTION)
        val batch = firestore.batch()
        quests.forEach { quest ->
            batch.set(collection.document(quest.id), quest.toDocument())
        }
        batch.commit().await()
    }

    suspend fun saveQuest(uid: String, quest: Quest) {
        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(QUESTS_SUBCOLLECTION)
            .document(quest.id)
            .set(quest.toDocument())
            .await()
    }

    suspend fun deleteQuest(uid: String, questId: String) {
        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(QUESTS_SUBCOLLECTION)
            .document(questId)
            .delete()
            .await()
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val QUESTS_SUBCOLLECTION = "quests"
    }
}
