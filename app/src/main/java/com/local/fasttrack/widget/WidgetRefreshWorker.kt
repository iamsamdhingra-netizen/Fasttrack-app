package com.local.fasttrack.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Android does not let a widget re-draw itself every second, and doing so would
 * drain the battery anyway. We refresh every 15 minutes (the platform's minimum
 * for periodic work) which is enough for an hours-long fasting timer + stage label.
 * The widget also updates immediately whenever you start/end a fast or log water
 * from inside the app (see updateFastingWidget calls in FastingViewModel).
 */
class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        updateFastingWidget(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "fasting_widget_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
