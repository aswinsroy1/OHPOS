package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.RestaurantProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val profileDao = AppDatabase.getDatabase(application).restaurantProfileDao()

    val profile: StateFlow<RestaurantProfile?> = profileDao.getProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveProfile(profile: RestaurantProfile) {
        viewModelScope.launch {
            profileDao.insertProfile(profile)
        }
    }
}
