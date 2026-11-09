package com.example.farmledger.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.farmledger.ui.dayIndex
import java.util.concurrent.TimeUnit

// Helper class for scheduling, canceling, and testing background WorkManager notifications.
class ReminderScheduler(private val context: Context) {

    // Schedules a WorkManager background notification for a specific reminder unless its due date has already passed.
    fun schedule(reminderId: Long, title: String, projectName: String, dueDateMs: Long) {
        val nowMs = System.currentTimeMillis()
        if (dayIndex(dueDateMs) < dayIndex(nowMs)) {
            return
        }

        val delayMs = (dueDateMs - nowMs).coerceAtLeast(0)
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(
                workDataOf(
                    "title" to title,
                    "projectName" to projectName
                )
            )
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder-$reminderId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    // Cancels any scheduled background notification work for a given reminder ID.
    fun cancel(reminderId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder-$reminderId")
    }

    // Enqueues a test notification scheduled to trigger in 15 seconds for demonstration purposes.
    fun scheduleTest() {
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(
                workDataOf(
                    "title" to "FarmLedger test reminder",
                    "projectName" to "Demo"
                )
            )
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "test-reminder",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
