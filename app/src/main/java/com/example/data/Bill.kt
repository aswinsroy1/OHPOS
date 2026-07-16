package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val totalAmount: Double,
    val totalItems: Int,
    val orderMode: String = "Restaurant",
    val printStatus: String = "SAVED",
    val isDeleted: Boolean = false,
    val state: String = "ACTIVE",
    val paymentMethod: String = "CASH",
    val gstAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val gstRatePercent: Double = 5.0
)

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val billId: Int,
    val menuItemId: Int,
    val menuItemName: String,
    val menuItemCategory: String,
    val quantity: Int,
    val price: Double
)
