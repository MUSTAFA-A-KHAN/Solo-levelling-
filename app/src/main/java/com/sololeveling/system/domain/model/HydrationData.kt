package com.sololeveling.system.domain.model

data class HydrationData(
    val dailyGoalLiters: Double = 2.0,
    val currentIntakeLiters: Double = 0.0,
    val lastDrinkTimestamp: Long = 0,
    val logs: List<HydrationLog> = emptyList(),
    val reminderEnabled: Boolean = false,
    val reminderIntervalMinutes: Int = 60
)

data class HydrationLog(
    val amountLiters: Double,
    val timestamp: Long = System.currentTimeMillis()
)
