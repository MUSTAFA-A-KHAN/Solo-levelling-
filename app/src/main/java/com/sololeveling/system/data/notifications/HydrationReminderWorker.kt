package com.sololeveling.system.data.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HydrationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: SystemNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val success = notificationManager.showNotification(
            title = "SYSTEM ALERT: HYDRATION",
            message = "Vitality levels dropping. Consume water to maintain peak performance.",
            notificationId = NOTIFICATION_ID
        )
        return if (success) Result.success() else Result.retry()
    }

    companion object {
        const val WORK_NAME = "hydration_reminder_work"
        const val NOTIFICATION_ID = 1001

        /**
         * Creates default constraints for the hydration reminder worker.
         */
        fun createConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresStorageNotLow(false)
                .build()
        }
    }
}
