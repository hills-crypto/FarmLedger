package com.example.farmledger.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmledger.data.Customer
import com.example.farmledger.data.EntryKind
import com.example.farmledger.data.FarmRepository
import com.example.farmledger.data.LedgerEntry
import com.example.farmledger.data.PaymentType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// Manages state, inputs, and validation for adding a new ledger expense or sale transaction.
class AddTransactionViewModel(
    private val repository: FarmRepository,
    private val projectId: Long
) : ViewModel() {

    var kind by mutableStateOf(EntryKind.EXPENSE)
        private set

    var category by mutableStateOf("Feed")
        private set

    var amount by mutableStateOf("")
        private set

    var date by mutableLongStateOf(System.currentTimeMillis())
        private set

    var note by mutableStateOf("")
        private set

    var quantity by mutableStateOf("1")
        private set

    var paymentType by mutableStateOf(PaymentType.CASH)
        private set

    var customerName by mutableStateOf("")
        private set

    var customerPhone by mutableStateOf("")
        private set

    var validationErrors by mutableStateOf(TransactionValidationResult())
        private set

    var isSaving by mutableStateOf(false)
        private set

    // Resets form inputs and error messages back to default state.
    fun reset() {
        kind = EntryKind.EXPENSE
        category = "Feed"
        amount = ""
        date = System.currentTimeMillis()
        note = ""
        quantity = "1"
        paymentType = PaymentType.CASH
        customerName = ""
        customerPhone = ""
        validationErrors = TransactionValidationResult()
        isSaving = false
    }

    // Updates transaction kind (Expense or Sale) and adjusts default categories.
    fun updateKind(newKind: EntryKind) {
        kind = newKind
        if (newKind == EntryKind.EXPENSE && category.isBlank()) {
            category = "Feed"
        }
        validationErrors = TransactionValidationResult()
    }

    // Updates expense category value.
    fun updateCategory(newCategory: String) {
        category = newCategory
    }

    // Updates transaction monetary amount string.
    fun updateAmount(input: String) {
        amount = input
    }

    // Updates transaction date timestamp.
    fun updateDate(newDateMs: Long) {
        date = newDateMs
    }

    // Updates optional transaction note string.
    fun updateNote(input: String) {
        note = input
    }

    // Updates quantity sold for sale transactions.
    fun updateQuantity(input: String) {
        quantity = input
    }

    // Updates payment mode (Cash or Credit).
    fun updatePaymentType(newPaymentType: PaymentType) {
        paymentType = newPaymentType
    }

    // Updates credit customer name string.
    fun updateCustomerName(input: String) {
        customerName = input
    }

    // Updates credit customer phone number string.
    fun updateCustomerPhone(input: String) {
        customerPhone = input
    }

    // Validates inputs and inserts the new ledger transaction into the database.
    fun save(onSaved: () -> Unit) {
        if (isSaving) return

        viewModelScope.launch {
            val project = repository.getProjectById(projectId).firstOrNull() ?: return@launch
            val quantitySold = repository.getQuantitySold(projectId).firstOrNull() ?: 0
            val animalsRemaining = (project.quantity - quantitySold).coerceAtLeast(0)

            val result = validateTransaction(
                kind = kind,
                category = category,
                amountStr = amount,
                dateMs = date,
                projectStartDateMs = project.startDate,
                currentDateMs = System.currentTimeMillis(),
                paymentType = paymentType,
                customerName = customerName,
                quantityStr = quantity,
                animalsRemaining = animalsRemaining,
                projectType = project.type
            )

            validationErrors = result
            if (!result.isValid) return@launch

            isSaving = true
            try {
                var assignedCustomerId: Long? = null
                if (kind == EntryKind.SALE && paymentType == PaymentType.CREDIT) {
                    val trimmedName = customerName.trim()
                    val existingCustomer = repository.findCustomerByName(trimmedName)
                    assignedCustomerId = existingCustomer?.id ?: repository.insertCustomer(
                        Customer(
                            name = trimmedName,
                            phone = customerPhone.trim()
                        )
                    )
                }

                val entry = LedgerEntry(
                    projectId = projectId,
                    kind = kind,
                    category = if (kind == EntryKind.EXPENSE) category.trim() else "Sale",
                    amount = amount.toDouble(),
                    quantity = if (kind == EntryKind.SALE) quantity.toInt() else 0,
                    date = date,
                    note = note.trim(),
                    paymentType = if (kind == EntryKind.SALE) paymentType else PaymentType.CASH,
                    customerId = assignedCustomerId
                )

                repository.insertTransaction(entry)
                reset()
                onSaved()
            } finally {
                isSaving = false
            }
        }
    }
}
