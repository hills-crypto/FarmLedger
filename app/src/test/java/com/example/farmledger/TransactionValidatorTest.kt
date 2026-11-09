package com.example.farmledger

import com.example.farmledger.data.EntryKind
import com.example.farmledger.data.PaymentType
import com.example.farmledger.data.ProjectType
import com.example.farmledger.ui.validateTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit test suite verifying input validation rules for transaction entries.
class TransactionValidatorTest {

    private val now = System.currentTimeMillis()
    private val start = now - (10 * 24 * 60 * 60 * 1000L) // 10 days ago

    // Passing test cases for each individual rule
    @Test
    fun validAmount_passes() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Feed",
            amountStr = "150.00",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertNull(result.amountError)
    }

    @Test
    fun validCategory_passes() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Drugs",
            amountStr = "50.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertNull(result.categoryError)
    }

    @Test
    fun validDate_passes() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Feed",
            amountStr = "100.0",
            dateMs = start + (2 * 24 * 60 * 60 * 1000L),
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertNull(result.dateError)
    }

    @Test
    fun validCreditCustomer_passes() {
        val result = validateTransaction(
            kind = EntryKind.SALE,
            category = "Sale",
            amountStr = "500.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CREDIT,
            customerName = "Mr Banda",
            quantityStr = "10",
            animalsRemaining = 50,
            projectType = ProjectType.POULTRY
        )
        assertNull(result.customerNameError)
    }

    @Test
    fun validQuantity_passes() {
        val result = validateTransaction(
            kind = EntryKind.SALE,
            category = "Sale",
            amountStr = "500.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "15",
            animalsRemaining = 50,
            projectType = ProjectType.POULTRY
        )
        assertNull(result.quantityError)
        assertTrue(result.isValid)
    }

    // Failing test cases for each rule and edge cases
    @Test
    fun invalidAmount_fails() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Feed",
            amountStr = "0.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Please enter a valid amount greater than zero.", result.amountError)
    }

    @Test
    fun missingExpenseCategory_fails() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "",
            amountStr = "100.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Please choose an expense category.", result.categoryError)
    }

    @Test
    fun dateBeforeProjectStart_fails() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Feed",
            amountStr = "100.0",
            dateMs = start - (2 * 24 * 60 * 60 * 1000L), // 2 days before project start
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Date must be between the project start date and today.", result.dateError)
    }

    @Test
    fun dateInFuture_fails() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Feed",
            amountStr = "100.0",
            dateMs = now + (2 * 24 * 60 * 60 * 1000L), // 2 days in future
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Date must be between the project start date and today.", result.dateError)
    }

    @Test
    fun creditSaleWithoutCustomerName_fails() {
        val result = validateTransaction(
            kind = EntryKind.SALE,
            category = "Sale",
            amountStr = "500.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CREDIT,
            customerName = "",
            quantityStr = "10",
            animalsRemaining = 50,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Enter the customer's name for a credit sale.", result.customerNameError)
    }

    @Test
    fun quantityZero_fails() {
        val result = validateTransaction(
            kind = EntryKind.SALE,
            category = "Sale",
            amountStr = "500.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 50,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Please enter a valid quantity greater than zero.", result.quantityError)
    }

    @Test
    fun quantityDecimal_fails() {
        val result = validateTransaction(
            kind = EntryKind.SALE,
            category = "Sale",
            amountStr = "500.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "2.5",
            animalsRemaining = 50,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Please enter a valid quantity greater than zero.", result.quantityError)
    }

    @Test
    fun saleExceedingAnimalsRemaining_fails() {
        val result = validateTransaction(
            kind = EntryKind.SALE,
            category = "Sale",
            amountStr = "500.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "60",
            animalsRemaining = 50,
            projectType = ProjectType.POULTRY
        )
        assertEquals("You only have 50 birds left in this batch.", result.quantityError)
    }
}
