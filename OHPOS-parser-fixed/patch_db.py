with open("app/src/main/java/com/example/data/Bill.kt", "r") as f:
    content = f.read()
content = content.replace("val isDeleted: Boolean = false", "val isDeleted: Boolean = false,\n    val state: String = \"ACTIVE\"")
with open("app/src/main/java/com/example/data/Bill.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/data/AppDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("version = 5,", "version = 6,")

migration_6 = """
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bills ADD COLUMN state TEXT NOT NULL DEFAULT 'ACTIVE'")
                db.execSQL("UPDATE bills SET state = 'PENDING_DELETION' WHERE isDeleted = 1")
            }
        }
"""
content = content.replace("private val MIGRATION_4_5", migration_6 + "\n        private val MIGRATION_4_5")
content = content.replace(".addMigrations(MIGRATION_4_5)", ".addMigrations(MIGRATION_4_5, MIGRATION_5_6)")

with open("app/src/main/java/com/example/data/AppDatabase.kt", "w") as f:
    f.write(content)
