package com.sololeveling.system.data.remote.firebase

import com.sololeveling.system.domain.model.LeaderboardEntry
import com.sololeveling.system.domain.model.Rank

data class LeaderboardDocument(
    val uid: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val rank: String = Rank.E.name,
    val level: Int = 1,
    val xp: Long = 0,
    val nextLevelXp: Long = 100,
    val totalAttributes: Double = 0.0,
    val completedQuests: Int = 0,
    val footsteps: Long = 0L,
    val score: Long = 0L,
    val lastUpdated: Long = 0L
)

fun LeaderboardDocument.toDomain(): LeaderboardEntry = LeaderboardEntry(
    uid = uid,
    displayName = displayName,
    photoUrl = photoUrl,
    rank = runCatching { Rank.valueOf(rank) }.getOrDefault(Rank.E),
    level = level,
    xp = xp,
    nextLevelXp = nextLevelXp,
    totalAttributes = totalAttributes,
    completedQuests = completedQuests,
    footsteps = footsteps,
    score = score,
    lastUpdated = lastUpdated
)

fun LeaderboardEntry.toDocument(): LeaderboardDocument = LeaderboardDocument(
    uid = uid,
    displayName = displayName,
    photoUrl = photoUrl,
    rank = rank.name,
    level = level,
    xp = xp,
    nextLevelXp = nextLevelXp,
    totalAttributes = totalAttributes,
    completedQuests = completedQuests,
    footsteps = footsteps,
    score = score,
    lastUpdated = lastUpdated
)
