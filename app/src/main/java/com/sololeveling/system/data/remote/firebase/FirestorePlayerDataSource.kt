package com.sololeveling.system.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.sololeveling.system.domain.model.Player
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePlayerDataSource @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getPlayer(uid: String): Player? {
        val docRef = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(PLAYER_SUBCOLLECTION)
            .document(PLAYER_DOC_ID)

        val snapshot = docRef.get().await()
        if (!snapshot.exists()) return null
        val doc = snapshot.toObject(PlayerDocument::class.java) ?: return null
        return doc.toDomain()
    }

    suspend fun savePlayer(uid: String, player: Player) {
        val docRef = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(PLAYER_SUBCOLLECTION)
            .document(PLAYER_DOC_ID)

        docRef.set(player.toDocument()).await()
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val PLAYER_SUBCOLLECTION = "player"
        private const val PLAYER_DOC_ID = "currentPlayer"
    }
}
