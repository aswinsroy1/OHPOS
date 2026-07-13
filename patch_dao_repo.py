import re

with open("app/src/main/java/com/example/data/MenuDao.kt", "r") as f:
    content = f.read()

content = content.replace("suspend fun getCount(): Int\n}", "suspend fun getCount(): Int\n\n    @Query(\"DELETE FROM menu_items WHERE id = :id\")\n    suspend fun deleteItem(id: Int)\n}")

with open("app/src/main/java/com/example/data/MenuDao.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/data/MenuRepository.kt", "r") as f:
    content = f.read()

content = content.replace("suspend fun updateAvailability(id: Int, isActive: Boolean) {", "suspend fun deleteItem(id: Int) {\n        menuDao.deleteItem(id)\n    }\n\n    suspend fun updateAvailability(id: Int, isActive: Boolean) {")

with open("app/src/main/java/com/example/data/MenuRepository.kt", "w") as f:
    f.write(content)

