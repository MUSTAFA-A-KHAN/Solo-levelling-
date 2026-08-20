package com.sololeveling.system.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        if (!isAvailable()) return false
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    private fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun getRecentSteps(since: Long): Long {
        if (!hasAllPermissions()) return 0

        val timeRange = TimeRangeFilter.after(Instant.ofEpochMilli(since))
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRange
            )
        )
        return response.records.sumOf { it.count }
    }

    suspend fun getRecentWorkoutDurationMinutes(since: Long): Long {
        if (!hasAllPermissions()) return 0

        val timeRange = TimeRangeFilter.after(Instant.ofEpochMilli(since))
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = timeRange
            )
        )

        // Sum durations in minutes
        return response.records.sumOf { record ->
            val durationMs = record.endTime.toEpochMilli() - record.startTime.toEpochMilli()
            durationMs / (1000 * 60)
        }
    }

    suspend fun getTodaySteps(): Long {
        if (!hasAllPermissions()) return 0
        val startOfDay = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val timeRange = TimeRangeFilter.after(startOfDay)
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRange
            )
        )
        return response.records.sumOf { it.count }
    }

    suspend fun getTodayWorkoutDurationMinutes(): Long {
        if (!hasAllPermissions()) return 0
        val startOfDay = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val timeRange = TimeRangeFilter.after(startOfDay)
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = timeRange
            )
        )

        // Sum durations in minutes
        return response.records.sumOf { record ->
            val durationMs = record.endTime.toEpochMilli() - record.startTime.toEpochMilli()
            durationMs / (1000 * 60)
        }
    }
}
