package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_items WHERE isActive = 1")
    fun getActiveMenuItems(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items")
    suspend fun getAllMenuItemsSync(): List<MenuItem>

    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Query("UPDATE menu_items SET isActive = :isActive WHERE id = :id")
    suspend fun updateAvailability(id: Int, isActive: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MenuItem>)

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun getCount(): Int

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteItem(id: Int)
}
