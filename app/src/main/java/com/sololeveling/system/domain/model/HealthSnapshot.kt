package com.sololeveling.system.domain.model

data class HealthSnapshot(
    val steps: Long = 0,
    val workoutMinutes: Long = 0,
    val sleepMinutes: Long = 0,
    val exerciseSessions: Long = 0,
    val firstActivityTime: Long? = null,
    val eveningSteps: Long = 0
)
