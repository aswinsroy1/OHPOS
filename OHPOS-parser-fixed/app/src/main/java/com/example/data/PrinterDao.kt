package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrinterDao {
    @Query("SELECT * FROM saved_printers")
    suspend fun getAllPrintersSync(): List<SavedPrinter>

    @Query("SELECT * FROM saved_printers")
    fun getAllPrinters(): Flow<List<SavedPrinter>>

    @Query("SELECT * FROM saved_printers WHERE isDefault = 1 LIMIT 1")
    fun getDefaultPrinter(): Flow<SavedPrinter?>

    @Query("SELECT * FROM saved_printers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPrinterSync(): SavedPrinter?

    @Insert
    suspend fun insertPrinter(printer: SavedPrinter): Long

    @Update
    suspend fun updatePrinter(printer: SavedPrinter)

    @Delete
    suspend fun deletePrinter(printer: SavedPrinter)

    @Query("UPDATE saved_printers SET isDefault = 0")
    suspend fun clearDefaultPrinters()
}
