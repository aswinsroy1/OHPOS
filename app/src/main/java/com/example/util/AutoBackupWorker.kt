package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class AutoBackupWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val success = AutomaticBackupManager.performAutomaticBackup(context)
            if (success) {
                AppNotificationManager.notifyBackupSuccess(
                    context,
                    "Automatic backup completed successfully"
                )
                Result.success()
            } else {
                AppNotificationManager.notifyBackupFailure(
                    context,
                    "Automatic backup failed — check storage permissions and backup folder"
                )
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppNotificationManager.notifyBackupFailure(
                context,
                "Automatic backup failed — ${e.message ?: "unknown error"}"
            )
            Result.retry()
        }
    }
}
