package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val formatNoDecimals = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun format(amount: Double): String {
        return format.format(amount)
    }

    fun format(amount: Float): String {
        return format.format(amount)
    }

    fun format(amount: Int): String {
        return format.format(amount)
    }

    fun formatNoDecimals(amount: Double): String {
        return formatNoDecimals.format(amount)
    }

    fun formatNoDecimals(amount: Float): String {
        return formatNoDecimals.format(amount)
    }

    fun formatNoDecimals(amount: Int): String {
        return formatNoDecimals.format(amount)
    }
}
