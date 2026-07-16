package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_closures")
data class DailyClosure(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateString: String,
    val closedAtTimestamp: Long,
    val totalOrders: Int,
    val totalSales: Double,
    val pdfFilePath: String?
)
