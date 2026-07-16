with open("app/src/main/java/com/example/data/BillDao.kt", "r") as f:
    content = f.read()

content = content.replace("WHERE isDeleted = 0 ORDER BY timestamp DESC", "ORDER BY timestamp DESC")
content = content.replace("WHERE isDeleted = 0", "")
content = content.replace("UPDATE bills SET isDeleted = 1 WHERE id = :billId", "UPDATE bills SET state = 'PENDING_DELETION' WHERE id = :billId")
content = content.replace("suspend fun moveToRecycleBin(billId: Int)", "suspend fun requestDeletion(billId: Int)")

content = content.replace("WHERE isDeleted = 1 ORDER BY timestamp DESC", "WHERE state = 'PENDING_DELETION' ORDER BY timestamp DESC")
content = content.replace("fun getDeletedBills(): Flow<List<BillWithItems>>", "fun getDeletionRequests(): Flow<List<BillWithItems>>")

content = content.replace("UPDATE bills SET isDeleted = 0 WHERE id = :billId", "UPDATE bills SET state = 'ACTIVE' WHERE id = :billId")
content = content.replace("suspend fun restoreBill(billId: Int)", "suspend fun rejectDeletion(billId: Int)")

with open("app/src/main/java/com/example/data/BillDao.kt", "w") as f:
    f.write(content)
