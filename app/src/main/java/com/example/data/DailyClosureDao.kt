package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyClosureDao {
    @Insert
    suspend fun insertClosure(closure: DailyClosure): Long

    @Query("SELECT * FROM daily_closures ORDER BY closedAtTimestamp DESC")
    fun getAllClosures(): Flow<List<DailyClosure>>

    @Query("SELECT * FROM daily_closures ORDER BY closedAtTimestamp DESC LIMIT 1")
    fun getLastClosureFlow(): Flow<DailyClosure?>

    @Query("SELECT * FROM daily_closures ORDER BY closedAtTimestamp DESC LIMIT 1")
    suspend fun getLastClosureSync(): DailyClosure?

    @Query("SELECT * FROM daily_closures WHERE dateString = :date LIMIT 1")
    suspend fun getClosureForDate(date: String): DailyClosure?
}
