package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val deliveryPrice: Double? = null,
    val category: String,
    val imageUrl: String = "",
    val isActive: Boolean = true
)
