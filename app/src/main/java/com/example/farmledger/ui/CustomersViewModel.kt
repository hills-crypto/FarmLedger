package com.example.farmledger.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmledger.data.CustomerBalance
import com.example.farmledger.data.FarmRepository
import com.example.farmledger.data.Payment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Holds the complete UI state for the customers list and total debt metrics.
data class CustomersUiState(
    val customers: List<CustomerBalance> = emptyList(),
    val totalOwed: Double = 0.0
)

// Manages business logic and data streams for customers, debt tracking, and payment recording.
class CustomersViewModel(private val repository: FarmRepository) : ViewModel() {

    // Reactive state flow that combines customer debt balances and overall total owed.
    val uiState: StateFlow<CustomersUiState> = combine(
        repository.getCustomersWithBalance(),
        repository.getTotalOwed()
    ) { customers, totalOwed ->
        CustomersUiState(customers = customers, totalOwed = totalOwed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CustomersUiState()
    )

    var paymentAmountError by mutableStateOf<String?>(null)
        private set

    var isSavingPayment by mutableStateOf(false)
        private set

    // Clears any active payment error message.
    fun clearError() {
        paymentAmountError = null
    }

    // Validates payment amount and records a customer cash debt payment.
    fun recordPayment(
        customerId: Long,
        amountStr: String,
        balance: Double,
        onDone: () -> Unit
    ) {
        if (isSavingPayment) return

        val error = validatePayment(amountStr, balance)
        paymentAmountError = error
        if (error != null) return

        isSavingPayment = true
        viewModelScope.launch {
            try {
                val parsedAmount = amountStr.toDouble()
                repository.insertPayment(
                    Payment(
                        customerId = customerId,
                        amount = parsedAmount,
                        date = System.currentTimeMillis()
                    )
                )
                paymentAmountError = null
                onDone()
            } finally {
                isSavingPayment = false
            }
        }
    }
}
