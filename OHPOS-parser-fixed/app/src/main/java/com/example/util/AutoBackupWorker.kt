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
            if (success) Result.success() else Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
