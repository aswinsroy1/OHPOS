package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dailyClosingDataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_closing_settings")

class DailyClosingPreferencesRepository(private val context: Context) {
    private val dataStore = context.dailyClosingDataStore
    
    private val AUTO_CLOSE_ENABLED = booleanPreferencesKey("auto_close_enabled")
    private val AUTO_CLOSE_HOUR = intPreferencesKey("auto_close_hour")
    private val AUTO_CLOSE_MINUTE = intPreferencesKey("auto_close_minute")
    private val EXPORT_FOLDER_URI = stringPreferencesKey("export_folder_uri")

    val isAutoCloseEnabled: Flow<Boolean> = dataStore.data.map { it[AUTO_CLOSE_ENABLED] ?: false }
    val autoCloseHour: Flow<Int> = dataStore.data.map { it[AUTO_CLOSE_HOUR] ?: 23 }
    val autoCloseMinute: Flow<Int> = dataStore.data.map { it[AUTO_CLOSE_MINUTE] ?: 59 }
    
    // Migrates from SharedPreferences if not found in DataStore
    val exportFolderUri: Flow<String?> = dataStore.data.map { prefs ->
        prefs[EXPORT_FOLDER_URI] ?: context.getSharedPreferences("ohpos_prefs", Context.MODE_PRIVATE).getString("report_folder_uri", null)
    }

    suspend fun setAutoCloseEnabled(enabled: Boolean) { dataStore.edit { it[AUTO_CLOSE_ENABLED] = enabled } }
    suspend fun setAutoCloseHour(hour: Int) { dataStore.edit { it[AUTO_CLOSE_HOUR] = hour } }
    suspend fun setAutoCloseMinute(minute: Int) { dataStore.edit { it[AUTO_CLOSE_MINUTE] = minute } }
    suspend fun setExportFolderUri(uri: String) { dataStore.edit { it[EXPORT_FOLDER_URI] = uri } }
}
