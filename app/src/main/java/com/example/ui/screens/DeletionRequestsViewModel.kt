package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BillDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeletionRequestsViewModel(application: Application) : AndroidViewModel(application) {
    private val billDao: BillDao

    init {
        val database = AppDatabase.getDatabase(application)
        billDao = database.billDao()
    }

    val pendingRequests = billDao.getDeletionRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun rejectDeletion(billId: Int) {
        viewModelScope.launch {
            billDao.rejectDeletion(billId)
        }
    }

    fun deleteBillPermanently(billId: Int) {
        viewModelScope.launch {
            billDao.deleteBillPermanently(billId)
        }
    }
}
