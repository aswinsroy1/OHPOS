package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.gstDataStore: DataStore<Preferences> by preferencesDataStore(name = "gst_settings")

class GstPreferencesRepository(private val context: Context) {
    private val dataStore = context.gstDataStore

    private val RESTAURANT_GST_PERCENT = doublePreferencesKey("restaurant_gst_percent")
    private val DELIVERY_GST_PERCENT = doublePreferencesKey("delivery_gst_percent")

    val restaurantGstPercent: Flow<Double> = dataStore.data.map { it[RESTAURANT_GST_PERCENT] ?: 5.0 }
    val deliveryGstPercent: Flow<Double> = dataStore.data.map { it[DELIVERY_GST_PERCENT] ?: 18.0 }

    suspend fun setRestaurantGstPercent(percent: Double) {
        dataStore.edit { it[RESTAURANT_GST_PERCENT] = percent }
    }

    suspend fun setDeliveryGstPercent(percent: Double) {
        dataStore.edit { it[DELIVERY_GST_PERCENT] = percent }
    }
}
