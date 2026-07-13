package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.room.withTransaction
import com.example.data.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import android.util.Log

data class BackupManifest(
    val app: String = "OH POS",
    val backupVersion: Int = 1,
    val appVersion: String,
    val created: String,
    val restaurantName: String,
    val device: String,
    val androidVersion: String,
    val menuItems: Int,
    val categories: Int,
    val orders: Int
)

object BackupRestoreManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun performBackup(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val menuItems = db.menuDao().getAllMenuItems().first()
            val bills = db.billDao().getBillsSync()
            val billItems = db.billDao().getAllBillItems().first()
            val restaurantProfile = db.restaurantProfileDao().getProfileSync()
            val printers = db.printerDao().getAllPrinters().first()
            
            val categories = menuItems.map { it.category }.distinct()

            val prefsStore = context.printerSettingsDataStore
            val prefsMap = prefsStore.data.first().asMap().mapKeys { it.key.name }

            val manifest = BackupManifest(
                appVersion = com.example.BuildConfig.VERSION_NAME,
                created = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date()),
                restaurantName = restaurantProfile?.name ?: "Unknown",
                device = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE,
                menuItems = menuItems.size,
                categories = categories.size,
                orders = bills.size
            )

            context.contentResolver.openOutputStream(uri)?.use { os ->
                ZipOutputStream(os).use { zip ->
                    
                    fun writeJsonToZip(filename: String, obj: Any) {
                        zip.putNextEntry(ZipEntry(filename))
                        val jsonBytes = gson.toJson(obj).toByteArray(Charsets.UTF_8)
                        zip.write(jsonBytes)
                        zip.closeEntry()
                    }

                    writeJsonToZip("manifest.json", manifest)
                    writeJsonToZip("restaurant.json", restaurantProfile ?: RestaurantProfile())
                    writeJsonToZip("menu.json", menuItems)
                    writeJsonToZip("categories.json", categories)
                    writeJsonToZip("orders.json", mapOf("bills" to bills, "billItems" to billItems))
                    writeJsonToZip("settings.json", prefsMap)
                    writeJsonToZip("printers.json", printers)
                    val securityPrefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                    writeJsonToZip("security.json", mapOf("pin_hash" to securityPrefs.getString("pin_hash", "")))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun inspectBackup(context: Context, uri: Uri): Result<BackupManifest> = withContext(Dispatchers.IO) {
        try {
            var manifest: BackupManifest? = null
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (entry.name == "manifest.json") {
                            val reader = InputStreamReader(zip, Charsets.UTF_8)
                            val jsonStr = reader.readText()
                            manifest = gson.fromJson(jsonStr, BackupManifest::class.java)
                            break
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            if (manifest != null && manifest!!.app == "OH POS") {
                Result.success(manifest!!)
            } else {
                Result.failure(Exception("Invalid backup file: manifest missing or incorrect app."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun executeRestore(context: Context, uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            var restaurant: RestaurantProfile? = null
            var menuItems: List<MenuItem>? = null
            var billsData: Map<String, Any>? = null
            var settingsData: Map<String, Any>? = null
            var printers: List<SavedPrinter>? = null
            var securityData: Map<String, String>? = null

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val reader = InputStreamReader(zip, Charsets.UTF_8)
                        val jsonStr = reader.readText()
                        when (entry.name) {
                            "restaurant.json" -> restaurant = gson.fromJson(jsonStr, RestaurantProfile::class.java)
                            "menu.json" -> menuItems = gson.fromJson(jsonStr, object : TypeToken<List<MenuItem>>() {}.type)
                            "orders.json" -> billsData = gson.fromJson(jsonStr, object : TypeToken<Map<String, Any>>() {}.type)
                            "settings.json" -> settingsData = gson.fromJson(jsonStr, object : TypeToken<Map<String, Any>>() {}.type)
                            "printers.json" -> printers = gson.fromJson(jsonStr, object : TypeToken<List<SavedPrinter>>() {}.type)
                            "security.json" -> securityData = gson.fromJson(jsonStr, object : TypeToken<Map<String, String>>() {}.type)
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            val db = AppDatabase.getDatabase(context)
            
            db.withTransaction {
                db.clearAllTables()
                if (restaurant != null) db.restaurantProfileDao().insertProfile(restaurant!!)
                if (menuItems != null) db.menuDao().insertAll(menuItems!!)
                if (billsData != null) {
                    val billsJson = gson.toJson(billsData!!["bills"])
                    val billItemsJson = gson.toJson(billsData!!["billItems"])
                    val bills = gson.fromJson<List<Bill>>(billsJson, object : TypeToken<List<Bill>>() {}.type)
                    val billItems = gson.fromJson<List<BillItem>>(billItemsJson, object : TypeToken<List<BillItem>>() {}.type)
                    
                    for(b in bills) db.billDao().insertBill(b)
                    if(billItems != null) db.billDao().insertBillItems(billItems)
                }
                if (printers != null) {
                    for (p in printers!!) db.printerDao().insertPrinter(p)
                }
            }

            // Outside transaction because DataStore/SharedPreferences don't support it directly
            if (settingsData != null) {
                context.printerSettingsDataStore.edit { prefs ->
                    settingsData!!.forEach { (k, v) ->
                        if (v is Boolean) prefs[booleanPreferencesKey(k)] = v
                        else if (v is Double) {
                            if (v % 1 == 0.0 && (k == "paper_size" || k == "auto_backup_count")) {
                                prefs[intPreferencesKey(k)] = v.toInt()
                            } else {
                                prefs[doublePreferencesKey(k)] = v
                            }
                        }
                        else if (v is String) prefs[stringPreferencesKey(k)] = v
                    }
                }
            }
            if (securityData != null) {
                val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                val pinHash = securityData!!["pin_hash"]
                if (!pinHash.isNullOrEmpty()) {
                    prefs.edit().putString("pin_hash", pinHash).apply()
                } else {
                    prefs.edit().remove("pin_hash").apply()
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
