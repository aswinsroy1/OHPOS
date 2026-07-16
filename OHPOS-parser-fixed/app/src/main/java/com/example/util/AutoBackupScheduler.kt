package com.example.util

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object AutoBackupScheduler {
    private const val WORK_NAME = "auto_backup_work"

    fun schedule(context: Context, frequencyHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            frequencyHours.toLong(), TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
