package com.sololeveling.system.data.repository

import com.sololeveling.system.data.local.dao.PlayerDao
import com.sololeveling.system.data.local.entity.toDomain
import com.sololeveling.system.data.local.entity.toEntity
import com.sololeveling.system.data.remote.firebase.FirestorePlayerDataSource
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.model.PlayerSyncResult
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao,
    private val firestorePlayerDataSource: FirestorePlayerDataSource,
    private val authRepository: AuthRepository
) : PlayerRepository {

    override fun getPlayer(): Flow<Player?> {
        return playerDao.getPlayer().map { it?.toDomain() }
    }

    override suspend fun updatePlayer(player: Player) {
        playerDao.updatePlayer(player.toEntity())
        authRepository.getCurrentUser()?.uid?.let { uid ->
            firestorePlayerDataSource.savePlayer(uid, player)
        }
    }

    override suspend fun initializePlayer(name: String) {
        // Prevent wiping out data if the player already exists
        val currentPlayer = playerDao.getPlayer().firstOrNull()
        if (currentPlayer == null) {
            // Set sync time to the start of today so they get credit for today's past activities
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val newPlayer = Player(name = name, lastSyncTime = startOfDay)
            playerDao.insertPlayer(newPlayer.toEntity())
        }
    }

    override suspend fun syncWithFirestore(uid: String): PlayerSyncResult {
        val localPlayer = playerDao.getPlayer().firstOrNull()?.toDomain()
        val remotePlayer = firestorePlayerDataSource.getPlayer(uid)

        return when {
            remotePlayer != null && localPlayer != null -> {
                if (isMeaningfulConflict(remotePlayer, localPlayer)) {
                    PlayerSyncResult.Conflict(remotePlayer, localPlayer)
                } else {
                    PlayerSyncResult.NoAction
                }
            }
            remotePlayer != null -> {
                playerDao.insertPlayer(remotePlayer.toEntity())
                PlayerSyncResult.DownloadedRemote
            }
            localPlayer != null -> {
                firestorePlayerDataSource.savePlayer(uid, localPlayer)
                PlayerSyncResult.UploadedLocal
            }
            else -> PlayerSyncResult.NoAction
        }
    }

    override suspend fun forceDownloadFromFirestore(uid: String) {
        val remotePlayer = firestorePlayerDataSource.getPlayer(uid) ?: return
        playerDao.insertPlayer(remotePlayer.toEntity())
    }

    private fun isMeaningfulConflict(remote: Player, local: Player): Boolean {
        return remote.copy(id = "", lastSyncTime = 0) != local.copy(id = "", lastSyncTime = 0)
    }
}
