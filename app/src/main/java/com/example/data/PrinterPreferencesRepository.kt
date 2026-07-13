package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.printerSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "printer_settings")

class PrinterPreferencesRepository(private val context: Context) {
    private val dataStore = context.printerSettingsDataStore
    
    // Core Print Settings
    private val PRINT_CUSTOMER_COPY = booleanPreferencesKey("print_customer_copy")
    private val PRINT_KITCHEN_COPY = booleanPreferencesKey("print_kitchen_copy")
    private val OPEN_DRAWER = booleanPreferencesKey("open_drawer")
    private val PAPER_SIZE = intPreferencesKey("paper_size") // 58 or 80

    // Receipt Layout Strings
    private val RES_NAME = stringPreferencesKey("res_name")
    private val RES_ADDRESS = stringPreferencesKey("res_address")
    private val RES_PHONE = stringPreferencesKey("res_phone")
    private val RES_GST = stringPreferencesKey("res_gst")
    private val INVOICE_FOOTER = stringPreferencesKey("invoice_footer")
    private val THANK_YOU_MSG = stringPreferencesKey("thank_you_msg")
    
    // Receipt Layout Booleans
    private val PRINT_DATE = booleanPreferencesKey("print_date")
    private val PRINT_TIME = booleanPreferencesKey("print_time")
    private val PRINT_CASHIER = booleanPreferencesKey("print_cashier")
    private val PRINT_PAYMENT_METHOD = booleanPreferencesKey("print_payment_method")
    private val PRINT_QR = booleanPreferencesKey("print_qr")
    private val PRINT_ORDER_TYPE = booleanPreferencesKey("print_order_type")
    private val PRINT_ITEM_NOTES = booleanPreferencesKey("print_item_notes")
    private val PRINT_GST_BREAKDOWN = booleanPreferencesKey("print_gst_breakdown")
    private val PRINT_DISCOUNT = booleanPreferencesKey("print_discount")
    private val PRINT_CUSTOMER_NAME = booleanPreferencesKey("print_customer_name")
    private val PRINT_CUSTOMER_PHONE = booleanPreferencesKey("print_customer_phone")
    
    // Flows - Core
    val printCustomerCopyFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_CUSTOMER_COPY] ?: false }
    val printKitchenCopyFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_KITCHEN_COPY] ?: false }
    val openDrawerFlow: Flow<Boolean> = dataStore.data.map { it[OPEN_DRAWER] ?: false }
    val paperSizeFlow: Flow<Int> = dataStore.data.map { it[PAPER_SIZE] ?: 58 }

    // Flows - Strings
    val resNameFlow: Flow<String> = dataStore.data.map { it[RES_NAME] ?: "" }
    val resAddressFlow: Flow<String> = dataStore.data.map { it[RES_ADDRESS] ?: "" }
    val resPhoneFlow: Flow<String> = dataStore.data.map { it[RES_PHONE] ?: "" }
    val resGstFlow: Flow<String> = dataStore.data.map { it[RES_GST] ?: "" }
    val invoiceFooterFlow: Flow<String> = dataStore.data.map { it[INVOICE_FOOTER] ?: "" }
    val thankYouMsgFlow: Flow<String> = dataStore.data.map { it[THANK_YOU_MSG] ?: "Thank you for visiting!" }

    // Flows - Booleans
    val printDateFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_DATE] ?: true }
    val printTimeFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_TIME] ?: true }
    val printCashierFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_CASHIER] ?: true }
    val printPaymentMethodFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_PAYMENT_METHOD] ?: true }
    val printQrFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_QR] ?: true }
    val printOrderTypeFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_ORDER_TYPE] ?: true }
    val printItemNotesFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_ITEM_NOTES] ?: true }
    val printGstBreakdownFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_GST_BREAKDOWN] ?: true }
    val printDiscountFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_DISCOUNT] ?: true }
    val printCustomerNameFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_CUSTOMER_NAME] ?: true }
    val printCustomerPhoneFlow: Flow<Boolean> = dataStore.data.map { it[PRINT_CUSTOMER_PHONE] ?: true }
    
    // Setters - Core
    suspend fun setPrintCustomerCopy(v: Boolean) { dataStore.edit { it[PRINT_CUSTOMER_COPY] = v } }
    suspend fun setPrintKitchenCopy(v: Boolean) { dataStore.edit { it[PRINT_KITCHEN_COPY] = v } }
    suspend fun setOpenDrawer(v: Boolean) { dataStore.edit { it[OPEN_DRAWER] = v } }
    suspend fun setPaperSize(v: Int) { dataStore.edit { it[PAPER_SIZE] = v } }

    // Setters - Strings
    suspend fun setResName(v: String) { dataStore.edit { it[RES_NAME] = v } }
    suspend fun setResAddress(v: String) { dataStore.edit { it[RES_ADDRESS] = v } }
    suspend fun setResPhone(v: String) { dataStore.edit { it[RES_PHONE] = v } }
    suspend fun setResGst(v: String) { dataStore.edit { it[RES_GST] = v } }
    suspend fun setInvoiceFooter(v: String) { dataStore.edit { it[INVOICE_FOOTER] = v } }
    suspend fun setThankYouMsg(v: String) { dataStore.edit { it[THANK_YOU_MSG] = v } }

    // Setters - Booleans
    suspend fun setPrintDate(v: Boolean) { dataStore.edit { it[PRINT_DATE] = v } }
    suspend fun setPrintTime(v: Boolean) { dataStore.edit { it[PRINT_TIME] = v } }
    suspend fun setPrintCashier(v: Boolean) { dataStore.edit { it[PRINT_CASHIER] = v } }
    suspend fun setPrintPaymentMethod(v: Boolean) { dataStore.edit { it[PRINT_PAYMENT_METHOD] = v } }
    suspend fun setPrintQr(v: Boolean) { dataStore.edit { it[PRINT_QR] = v } }
    suspend fun setPrintOrderType(v: Boolean) { dataStore.edit { it[PRINT_ORDER_TYPE] = v } }
    suspend fun setPrintItemNotes(v: Boolean) { dataStore.edit { it[PRINT_ITEM_NOTES] = v } }
    suspend fun setPrintGstBreakdown(v: Boolean) { dataStore.edit { it[PRINT_GST_BREAKDOWN] = v } }
    suspend fun setPrintDiscount(v: Boolean) { dataStore.edit { it[PRINT_DISCOUNT] = v } }
    suspend fun setPrintCustomerName(v: Boolean) { dataStore.edit { it[PRINT_CUSTOMER_NAME] = v } }
    suspend fun setPrintCustomerPhone(v: Boolean) { dataStore.edit { it[PRINT_CUSTOMER_PHONE] = v } }
}
