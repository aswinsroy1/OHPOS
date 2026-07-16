package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantProfileDao {
    @Query("SELECT * FROM restaurant_profile WHERE id = 1")
    fun getProfile(): Flow<RestaurantProfile?>

    @Query("SELECT * FROM restaurant_profile WHERE id = 1")
    suspend fun getProfileSync(): RestaurantProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: RestaurantProfile)
}
