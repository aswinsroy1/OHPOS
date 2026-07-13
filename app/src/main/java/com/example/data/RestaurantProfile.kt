package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_profile")
data class RestaurantProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val businessType: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstNumber: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
    val logoUri: String? = null
)
