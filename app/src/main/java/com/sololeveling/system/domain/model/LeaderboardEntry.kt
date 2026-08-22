package com.sololeveling.system.domain.model

data class LeaderboardEntry(
    val uid: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val rank: Rank = Rank.E,
    val level: Int = 1,
    val xp: Long = 0,
    val nextLevelXp: Long = 100,
    val totalAttributes: Double = 60.0,
    val completedQuests: Int = 0,
    val score: Long = 0L,
    val lastUpdated: Long = 0L
)
