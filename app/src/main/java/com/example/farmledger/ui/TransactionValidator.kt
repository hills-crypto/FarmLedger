package com.example.farmledger.ui

import com.example.farmledger.data.EntryKind
import com.example.farmledger.data.PaymentType
import com.example.farmledger.data.ProjectType

// Data class encapsulating field validation errors for a ledger transaction entry form.
data class TransactionValidationResult(
    val amountError: String? = null,
    val categoryError: String? = null,
    val dateError: String? = null,
    val customerNameError: String? = null,
    val quantityError: String? = null
) {
    // Indicates if all form fields passed validation without any errors.
    val isValid: Boolean
        get() = amountError == null && categoryError == null && dateError == null && customerNameError == null && quantityError == null
}

// Pure function that validates transaction input fields against project constraints and business rules.
fun validateTransaction(
    kind: EntryKind,
    category: String,
    amountStr: String,
    dateMs: Long,
    projectStartDateMs: Long,
    currentDateMs: Long,
    paymentType: PaymentType,
    customerName: String,
    quantityStr: String,
    animalsRemaining: Int,
    projectType: ProjectType
): TransactionValidationResult {
    var amountErr: String? = null
    var categoryErr: String? = null
    var dateErr: String? = null
    var customerNameErr: String? = null
    var quantityErr: String? = null

    // Amount validation
    val parsedAmount = amountStr.toDoubleOrNull()
    if (parsedAmount == null || parsedAmount <= 0.0) {
        amountErr = "Please enter a valid amount greater than zero."
    }

    // Category validation
    if (kind == EntryKind.EXPENSE && category.isBlank()) {
        categoryErr = "Please choose an expense category."
    }

    // Date validation
    if (dateMs > currentDateMs || dateMs < projectStartDateMs) {
        dateErr = "Date must be between the project start date and today."
    }

    // Customer name validation
    if (kind == EntryKind.SALE && paymentType == PaymentType.CREDIT && customerName.isBlank()) {
        customerNameErr = "Enter the customer's name for a credit sale."
    }

    // Quantity validation
    if (kind == EntryKind.SALE) {
        val parsedQuantity = quantityStr.toIntOrNull()
        if (parsedQuantity == null || parsedQuantity <= 0) {
            quantityErr = "Please enter a valid quantity greater than zero."
        } else if (parsedQuantity > animalsRemaining) {
            val unitName = when (projectType) {
                ProjectType.POULTRY -> "birds"
                ProjectType.GOATS -> "goats"
                ProjectType.GARDEN -> "units"
            }
            quantityErr = "You only have $animalsRemaining $unitName left in this batch."
        }
    }

    return TransactionValidationResult(
        amountError = amountErr,
        categoryError = categoryErr,
        dateError = dateErr,
        customerNameError = customerNameErr,
        quantityError = quantityErr
    )
}
