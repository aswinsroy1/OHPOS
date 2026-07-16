package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed repository for per-category notification toggles.
 * Uses the existing printer_settings DataStore to avoid creating a
 * second DataStore instance (Android enforces one-per-file).
 */
class NotificationPreferencesRepository(private val context: Context) {
    private val dataStore = context.printerSettingsDataStore

    companion object {
        private val NOTIFY_BACKUP_SUCCESS = booleanPreferencesKey("notify_backup_success")
        private val NOTIFY_BACKUP_FAILURE = booleanPreferencesKey("notify_backup_failure")
        private val NOTIFY_PRINTER_FAILURE = booleanPreferencesKey("notify_printer_failure")
        private val NOTIFY_DELETION_REQUEST = booleanPreferencesKey("notify_deletion_request")
    }

    // Flows — all default to true (ON)
    val backupSuccessEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFY_BACKUP_SUCCESS] ?: true }
    val backupFailureEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFY_BACKUP_FAILURE] ?: true }
    val printerFailureEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFY_PRINTER_FAILURE] ?: true }
    val deletionRequestEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFY_DELETION_REQUEST] ?: true }

    // Setters
    suspend fun setBackupSuccessEnabled(v: Boolean) { dataStore.edit { it[NOTIFY_BACKUP_SUCCESS] = v } }
    suspend fun setBackupFailureEnabled(v: Boolean) { dataStore.edit { it[NOTIFY_BACKUP_FAILURE] = v } }
    suspend fun setPrinterFailureEnabled(v: Boolean) { dataStore.edit { it[NOTIFY_PRINTER_FAILURE] = v } }
    suspend fun setDeletionRequestEnabled(v: Boolean) { dataStore.edit { it[NOTIFY_DELETION_REQUEST] = v } }
}
