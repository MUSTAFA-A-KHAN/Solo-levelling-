package com.sololeveling.system.data.remote.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sololeveling.system.domain.model.LeaderboardEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreLeaderboardDataSource @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    fun getLeaderboard(): kotlinx.coroutines.flow.Flow<List<LeaderboardEntry>> = callbackFlow {
        val listener = firestore
            .collection(LEADERBOARD_COLLECTION)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(LEADERBOARD_LIMIT)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Leaderboard snapshot listener failed", error)
                    trySend(emptyList<LeaderboardEntry>())
                } else {
                    val entries = snapshots?.documents?.mapNotNull { doc ->
                        doc.toObject(LeaderboardDocument::class.java)?.toDomain()
                    } ?: emptyList()
                    trySend(entries)
                }
            }

        awaitClose {
            try {
                listener.remove()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to remove leaderboard listener", e)
            }
        }
    }.distinctUntilChanged()

    suspend fun upsertEntry(entry: LeaderboardEntry) {
        firestore
            .collection(LEADERBOARD_COLLECTION)
            .document(entry.uid)
            .set(entry.toDocument())
            .await()
    }

    companion object {
        private const val TAG = "FirestoreLeaderboard"
        private const val LEADERBOARD_COLLECTION = "leaderboard"
        private const val LEADERBOARD_LIMIT: Long = 100
    }
}
