package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import android.util.Base64

class PinManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    fun hasPin(): Boolean {
        return prefs.contains("pin_hash")
    }

    fun setPin(pin: String) {
        val salt = "OHPOS_SALT_99" // A static salt is fine for a simple offline app
        val hash = hashString(pin + salt)
        prefs.edit().putString("pin_hash", hash).apply()
    }

    fun clearPin() {
        prefs.edit().remove("pin_hash").apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = "OHPOS_SALT_99"
        val hash = hashString(pin + salt)
        return hash == prefs.getString("pin_hash", "")
    }

    private fun hashString(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}
