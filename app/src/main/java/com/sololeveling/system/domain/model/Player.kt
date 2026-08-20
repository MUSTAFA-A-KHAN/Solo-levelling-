package com.sololeveling.system.domain.model

data class Player(
    val id: String = "PLAYER_1",
    val name: String = "Player",
    val title: String? = null,
    val level: Int = 1,
    val xp: Long = 0,
    val nextLevelXp: Long = 100, // Determined by progression curve
    val rank: Rank = Rank.E,
    val attributes: PlayerAttributes = PlayerAttributes(),
    val availableAttributePoints: Int = 0
)

data class PlayerAttributes(
    val strength: Double = 10.0,
    val agility: Double = 10.0,
    val vitality: Double = 10.0,
    val intelligence: Double = 10.0,
    val discipline: Double = 10.0,
    val endurance: Double = 10.0
)

enum class Rank(val value: Int, val title: String) {
    E(1, "E-Rank"),
    D(2, "D-Rank"),
    C(3, "C-Rank"),
    B(4, "B-Rank"),
    A(5, "A-Rank"),
    S(6, "S-Rank")
}
