package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.NotificationPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Central notification manager for OH POS.
 *
 * Creates Android notification channels (one per category) and exposes
 * per-category notify methods that respect the user's DataStore toggles
 * and the POST_NOTIFICATIONS runtime permission (API 33+).
 */
object AppNotificationManager {

    // Channel IDs
    private const val CHANNEL_BACKUP_SUCCESS = "backup_success"
    private const val CHANNEL_BACKUP_FAILURE = "backup_failure"
    private const val CHANNEL_PRINTER_FAILURE = "printer_failure"
    private const val CHANNEL_DELETION_REQUEST = "deletion_request"

    // Notification IDs (unique per category so they don't overwrite each other)
    private const val ID_BACKUP_SUCCESS = 1001
    private const val ID_BACKUP_FAILURE = 1002
    private const val ID_PRINTER_FAILURE = 1003
    private var deletionRequestIdCounter = 2000

    /**
     * Create all notification channels. Safe to call multiple times —
     * Android ignores duplicate channel creation.
     * Should be called once from MainActivity.onCreate().
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_BACKUP_SUCCESS,
                "Backup Complete",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications when a backup completes successfully"
            },
            NotificationChannel(
                CHANNEL_BACKUP_FAILURE,
                "Backup Failed",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a backup fails"
            },
            NotificationChannel(
                CHANNEL_PRINTER_FAILURE,
                "Print Failure",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a print job fails or the printer is unreachable"
            },
            NotificationChannel(
                CHANNEL_DELETION_REQUEST,
                "Deletion Requests",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when a new deletion request needs manager approval"
            }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    /**
     * Check if POST_NOTIFICATIONS permission is granted.
     * Always returns true on API < 33 (permission not required).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // ---- Per-category notification methods ----

    fun notifyBackupSuccess(context: Context, message: String) {
        val prefs = NotificationPreferencesRepository(context)
        val enabled = runBlocking { prefs.backupSuccessEnabled.first() }
        if (!enabled) return

        post(
            context = context,
            channelId = CHANNEL_BACKUP_SUCCESS,
            notificationId = ID_BACKUP_SUCCESS,
            title = "Backup Complete",
            message = message,
            priority = NotificationCompat.PRIORITY_LOW
        )
    }

    fun notifyBackupFailure(context: Context, message: String) {
        val prefs = NotificationPreferencesRepository(context)
        val enabled = runBlocking { prefs.backupFailureEnabled.first() }
        if (!enabled) return

        post(
            context = context,
            channelId = CHANNEL_BACKUP_FAILURE,
            notificationId = ID_BACKUP_FAILURE,
            title = "Backup Failed",
            message = message,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    fun notifyPrinterFailure(context: Context, message: String) {
        val prefs = NotificationPreferencesRepository(context)
        val enabled = runBlocking { prefs.printerFailureEnabled.first() }
        if (!enabled) return

        post(
            context = context,
            channelId = CHANNEL_PRINTER_FAILURE,
            notificationId = ID_PRINTER_FAILURE,
            title = "Print Failed",
            message = message,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    fun notifyDeletionRequest(context: Context, message: String) {
        val prefs = NotificationPreferencesRepository(context)
        val enabled = runBlocking { prefs.deletionRequestEnabled.first() }
        if (!enabled) return

        // Use incrementing IDs so multiple deletion requests don't overwrite each other
        post(
            context = context,
            channelId = CHANNEL_DELETION_REQUEST,
            notificationId = deletionRequestIdCounter++,
            title = "Deletion Request Pending",
            message = message,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    // ---- Internal ----

    private fun post(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        priority: Int
    ) {
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission was revoked between check and post — safe to ignore
            e.printStackTrace()
        }
    }
}
