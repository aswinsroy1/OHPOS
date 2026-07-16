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
import android.app.PendingIntent
import android.content.Intent

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
    private const val CHANNEL_DAY_CLOSING = "day_closing"

    // Notification IDs (unique per category so they don't overwrite each other)
    private const val ID_BACKUP_SUCCESS = 1001
    private const val ID_BACKUP_FAILURE = 1002
    private const val ID_PRINTER_FAILURE = 1003
    private const val ID_DAY_CLOSING = 1004
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
            },
            NotificationChannel(
                CHANNEL_DAY_CLOSING,
                "Daily Closing",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when a business day closes automatically"
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
    
    private fun createTargetIntent(context: Context, target: String): PendingIntent {
        val intent = Intent().apply {
            component = android.content.ComponentName(context, "com.example.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("extra_notification_target", target)
        }
        return PendingIntent.getActivity(
            context,
            target.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
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
            priority = NotificationCompat.PRIORITY_LOW,
            pendingIntent = createTargetIntent(context, "backup_settings")
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
            priority = NotificationCompat.PRIORITY_HIGH,
            pendingIntent = createTargetIntent(context, "backup_settings")
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
            priority = NotificationCompat.PRIORITY_HIGH,
            pendingIntent = createTargetIntent(context, "printer_settings")
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
            priority = NotificationCompat.PRIORITY_DEFAULT,
            pendingIntent = createTargetIntent(context, "deletion_requests")
        )
    }
    
    fun notifyDayClosed(context: Context, message: String, pdfUriStr: String?) {
        val pendingIntent = pdfUriStr?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(android.net.Uri.parse(it), "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                PendingIntent.getActivity(
                    context,
                    ID_DAY_CLOSING,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        post(
            context = context,
            channelId = CHANNEL_DAY_CLOSING,
            notificationId = ID_DAY_CLOSING,
            title = "Day Closed",
            message = message,
            priority = NotificationCompat.PRIORITY_DEFAULT,
            pendingIntent = pendingIntent
        )
    }

    // ---- Internal ----

    private fun post(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        priority: Int,
        pendingIntent: PendingIntent? = null
    ) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            
        pendingIntent?.let {
            builder.setContentIntent(it)
        }
            
        val notification = builder.build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission was revoked between check and post — safe to ignore
            e.printStackTrace()
        }
    }
}
