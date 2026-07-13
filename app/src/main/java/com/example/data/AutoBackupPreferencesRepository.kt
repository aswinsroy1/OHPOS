package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.autoBackupDataStore: DataStore<Preferences> by preferencesDataStore(name = "auto_backup_settings")

class AutoBackupPreferencesRepository(private val context: Context) {
    private val dataStore = context.autoBackupDataStore
    
    private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
    private val BACKUP_FOLDER_URI = stringPreferencesKey("backup_folder_uri")
    private val BACKUP_FREQUENCY_HOURS = intPreferencesKey("backup_frequency_hours")
    private val BACKUP_RETENTION = intPreferencesKey("backup_retention")
    private val LAST_SUCCESSFUL_BACKUP = longPreferencesKey("last_successful_backup")
    private val NEXT_SCHEDULED_BACKUP = longPreferencesKey("next_scheduled_backup")

    val isAutoBackupEnabled: Flow<Boolean> = dataStore.data.map { it[AUTO_BACKUP_ENABLED] ?: false }
    val backupFolderUri: Flow<String?> = dataStore.data.map { it[BACKUP_FOLDER_URI] }
    val backupFrequencyHours: Flow<Int> = dataStore.data.map { it[BACKUP_FREQUENCY_HOURS] ?: 24 }
    val backupRetention: Flow<Int> = dataStore.data.map { it[BACKUP_RETENTION] ?: 10 }
    val lastSuccessfulBackup: Flow<Long> = dataStore.data.map { it[LAST_SUCCESSFUL_BACKUP] ?: 0L }
    val nextScheduledBackup: Flow<Long> = dataStore.data.map { it[NEXT_SCHEDULED_BACKUP] ?: 0L }

    suspend fun setAutoBackupEnabled(enabled: Boolean) { dataStore.edit { it[AUTO_BACKUP_ENABLED] = enabled } }
    suspend fun setBackupFolderUri(uri: String) { dataStore.edit { it[BACKUP_FOLDER_URI] = uri } }
    suspend fun setBackupFrequencyHours(hours: Int) { dataStore.edit { it[BACKUP_FREQUENCY_HOURS] = hours } }
    suspend fun setBackupRetention(retention: Int) { dataStore.edit { it[BACKUP_RETENTION] = retention } }
    suspend fun setLastSuccessfulBackup(time: Long) { dataStore.edit { it[LAST_SUCCESSFUL_BACKUP] = time } }
    suspend fun setNextScheduledBackup(time: Long) { dataStore.edit { it[NEXT_SCHEDULED_BACKUP] = time } }
}
