package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.appearanceSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "appearance_settings")

class AppearancePreferencesRepository(private val context: Context) {
    private val dataStore = context.appearanceSettingsDataStore

    private val THEME_PREF = stringPreferencesKey("theme_pref") // Dark, Light, Pure Black, System Default
    private val ACCENT_COLOUR = stringPreferencesKey("accent_colour") // Lavender, Blue, Emerald, Orange, Red, Pink, Cyan

    val themeFlow: Flow<String> = dataStore.data.map { it[THEME_PREF] ?: "Dark" }
    val accentColourFlow: Flow<String> = dataStore.data.map { it[ACCENT_COLOUR] ?: "Lavender" }

    suspend fun setThemePref(theme: String) {
        dataStore.edit { it[THEME_PREF] = theme }
    }

    suspend fun setAccentColour(colour: String) {
        dataStore.edit { it[ACCENT_COLOUR] = colour }
    }
}
