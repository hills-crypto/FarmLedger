package com.example.farmledger.ui

// Pure function that validates debt payment input against customer balance constraints.
fun validatePayment(amountStr: String, balance: Double): String? {
    val parsedAmount = amountStr.toDoubleOrNull()
    if (parsedAmount == null || parsedAmount <= 0.0) {
        return "Please enter a valid amount greater than zero."
    }
    if (parsedAmount > balance) {
        return "Payment is more than the balance of ${formatCurrency(balance)}."
    }
    return null
}
