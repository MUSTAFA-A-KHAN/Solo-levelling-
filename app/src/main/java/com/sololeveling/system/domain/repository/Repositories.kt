package com.sololeveling.system.domain.repository

import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerSyncResult
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.model.SystemEvent
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayer(): Flow<Player?>
    suspend fun updatePlayer(player: Player)
    suspend fun initializePlayer(name: String)
    suspend fun syncWithFirestore(uid: String): PlayerSyncResult
    suspend fun forceDownloadFromFirestore(uid: String)
}

interface QuestRepository {
    fun getActiveQuests(): Flow<List<Quest>>
    fun getCompletedQuests(): Flow<List<Quest>>
    suspend fun updateQuest(quest: Quest)
    suspend fun addQuest(quest: Quest)
}

interface SystemEventRepository {
    fun getRecentEvents(): Flow<List<SystemEvent>>
    fun getUnreadEventCount(): Flow<Int>
    suspend fun addEvent(event: SystemEvent)
    suspend fun markAllAsRead()
}

interface AuthRepository {
    val authState: Flow<FirebaseUser?>
    fun getCurrentUser(): FirebaseUser?
    fun getGoogleSignInIntent(): Intent
    suspend fun handleSignInResult(data: Intent?): Result<FirebaseUser>
    suspend fun signOut()
    suspend fun syncAccountOnLaunch()
}

interface UserRepository {
    suspend fun createOrUpdateAccount(user: FirebaseUser)
}
