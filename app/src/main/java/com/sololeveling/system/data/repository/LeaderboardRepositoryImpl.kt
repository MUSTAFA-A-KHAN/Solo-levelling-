package com.sololeveling.system.data.repository

import com.sololeveling.system.data.remote.firebase.FirestoreLeaderboardDataSource
import com.sololeveling.system.domain.model.LeaderboardEntry
import com.sololeveling.system.domain.model.Player
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.LeaderboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val firestoreLeaderboardDataSource: FirestoreLeaderboardDataSource,
    private val authRepository: AuthRepository
) : LeaderboardRepository {

    override fun getLeaderboard(): Flow<List<LeaderboardEntry>> {
        return firestoreLeaderboardDataSource.getLeaderboard()
    }

    override suspend fun updateMyEntry(player: Player, completedQuests: Int) {
        val user = authRepository.getCurrentUser() ?: return

        val totalAttributes = with(player.attributes) {
            strength + agility + vitality + intelligence + discipline + endurance
        }

        val score = player.level * SCORE_LEVEL_WEIGHT + player.xp

        val entry = LeaderboardEntry(
            uid = user.uid,
            displayName = user.displayName ?: player.name,
            photoUrl = user.photoUrl?.toString() ?: "",
            rank = player.rank,
            level = player.level,
            xp = player.xp,
            nextLevelXp = player.nextLevelXp,
            totalAttributes = totalAttributes,
            completedQuests = completedQuests,
            score = score,
            lastUpdated = System.currentTimeMillis()
        )

        firestoreLeaderboardDataSource.upsertEntry(entry)
    }

    companion object {
        private const val SCORE_LEVEL_WEIGHT = 1_000_000L
    }
}
