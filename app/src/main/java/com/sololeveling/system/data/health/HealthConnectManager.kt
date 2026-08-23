package com.sololeveling.system.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.SleepSessionRecord
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
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        if (!isAvailable()) return false
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    private fun isAvailable(): Boolean {
        return HealthConnectClient.sdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    fun isHealthConnectAvailable(): Boolean = isAvailable()

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

    suspend fun getRecentCaloriesBurned(since: Long): Double {
        if (!hasAllPermissions()) return 0.0

        val timeRange = TimeRangeFilter.after(Instant.ofEpochMilli(since))
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = timeRange
            )
        )
        return response.records.sumOf { it.energy.inKilocalories }
    }

    suspend fun getRecentSleepDurationMinutes(since: Long): Long {
        if (!hasAllPermissions()) return 0

        val timeRange = TimeRangeFilter.after(Instant.ofEpochMilli(since))
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = timeRange
            )
        )

        return response.records.sumOf { record ->
            val durationMs = record.endTime.toEpochMilli() - record.startTime.toEpochMilli()
            durationMs / (1000 * 60)
        }
    }

    suspend fun getRecentExerciseSessionCount(since: Long): Long {
        if (!hasAllPermissions()) return 0

        val timeRange = TimeRangeFilter.after(Instant.ofEpochMilli(since))
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = timeRange
            )
        )
        return response.records.size.toLong()
    }

    suspend fun getEarliestActivityStartTime(since: Long): Long? {
        if (!hasAllPermissions()) return null

        val timeRange = TimeRangeFilter.after(Instant.ofEpochMilli(since))
        val stepResponse = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = StepsRecord::class, timeRangeFilter = timeRange)
        )
        val exerciseResponse = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = timeRange)
        )

        val startTimes = buildList {
            stepResponse.records.mapTo(this) { it.startTime.toEpochMilli() }
            exerciseResponse.records.mapTo(this) { it.startTime.toEpochMilli() }
        }
        return startTimes.minOrNull()
    }

    suspend fun getStepsInRange(startMs: Long, endMs: Long): Long {
        if (!hasAllPermissions()) return 0

        val timeRange = TimeRangeFilter.between(
            Instant.ofEpochMilli(startMs),
            Instant.ofEpochMilli(endMs)
        )
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(recordType = StepsRecord::class, timeRangeFilter = timeRange)
        )
        return response.records.sumOf { it.count }
    }
}
