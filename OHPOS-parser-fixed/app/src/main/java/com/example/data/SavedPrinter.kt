package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_printers")
data class SavedPrinter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // "WIFI", "BLUETOOTH", "USB"
    val address: String, // IP for wifi, MAC for bluetooth, identifier for USB
    val isDefault: Boolean = false,
    val paperWidth: Int = 58 // 58 or 80
)
