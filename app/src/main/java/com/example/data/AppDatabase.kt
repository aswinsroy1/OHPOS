package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MenuItem::class, Bill::class, BillItem::class, SavedPrinter::class, RestaurantProfile::class, DailyClosure::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun billDao(): BillDao
    abstract fun printerDao(): PrinterDao
    abstract fun restaurantProfileDao(): RestaurantProfileDao
    abstract fun dailyClosureDao(): DailyClosureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bills ADD COLUMN gstRatePercent REAL NOT NULL DEFAULT 5.0")
            }
        }
        
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `daily_closures` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
                        `dateString` TEXT NOT NULL, 
                        `closedAtTimestamp` INTEGER NOT NULL, 
                        `totalOrders` INTEGER NOT NULL, 
                        `totalSales` REAL NOT NULL, 
                        `pdfFilePath` TEXT
                    )
                """.trimIndent())
                
                db.execSQL("ALTER TABLE bills ADD COLUMN paymentMethod TEXT NOT NULL DEFAULT 'CASH'")
                db.execSQL("ALTER TABLE bills ADD COLUMN gstAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE bills ADD COLUMN discountAmount REAL NOT NULL DEFAULT 0.0")
            }
        }
        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `restaurant_profile` (
                        `id` INTEGER NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `businessType` TEXT NOT NULL, 
                        `phone` TEXT NOT NULL, 
                        `email` TEXT NOT NULL, 
                        `address` TEXT NOT NULL, 
                        `gstNumber` TEXT NOT NULL, 
                        `openingTime` TEXT NOT NULL, 
                        `closingTime` TEXT NOT NULL, 
                        `logoUri` TEXT, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }
        
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No-op to preserve null deliveryPrices
            }
        }
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bills ADD COLUMN state TEXT NOT NULL DEFAULT 'ACTIVE'")
                db.execSQL("UPDATE bills SET state = 'PENDING_DELETION' WHERE isDeleted = 1")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE menu_items ADD COLUMN deliveryPrice REAL")
                database.execSQL("ALTER TABLE bills ADD COLUMN orderMode TEXT NOT NULL DEFAULT 'Restaurant'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
