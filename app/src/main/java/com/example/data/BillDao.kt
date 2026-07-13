package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert
    suspend fun insertBill(bill: Bill): Long

    @Insert
    suspend fun insertBillItems(billItems: List<BillItem>)

    @Transaction
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    fun getAllBills(): Flow<List<BillWithItems>>

    @Query("SELECT * FROM bill_items")
    suspend fun getAllBillItemsSync(): List<BillItem>

    @Query("SELECT * FROM bill_items")
    fun getAllBillItems(): Flow<List<BillItem>>
    
    @Query("SELECT * FROM bills ")
    fun getBillsSync(): List<Bill>

    @Query("UPDATE bills SET printStatus = :status WHERE id = :billId")
    suspend fun updatePrintStatus(billId: Int, status: String)

    @Query("UPDATE bills SET state = 'PENDING_DELETION' WHERE id = :billId")
    suspend fun requestDeletion(billId: Int)

    @Transaction
    @Query("SELECT * FROM bills WHERE state = 'PENDING_DELETION' ORDER BY timestamp DESC")
    fun getDeletionRequests(): Flow<List<BillWithItems>>

    @Query("UPDATE bills SET state = 'ACTIVE' WHERE id = :billId")
    suspend fun rejectDeletion(billId: Int)

    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBillPermanently(billId: Int)
}
