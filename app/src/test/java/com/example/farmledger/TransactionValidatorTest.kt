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

    // Verifies that a valid expense transaction passes validation without errors.
    @Test
    fun validExpense_passes() {
        val result = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Feed",
            amountStr = "150.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertTrue(result.isValid)
    }

    // Verifies that an invalid or zero amount returns an error message.
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

    // Verifies that a blank expense category produces an error.
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

    // Verifies that transaction date in the future or before project start fails validation.
    @Test
    fun invalidDate_fails() {
        val futureResult = validateTransaction(
            kind = EntryKind.EXPENSE,
            category = "Feed",
            amountStr = "100.0",
            dateMs = now + (24 * 60 * 60 * 1000L),
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CASH,
            customerName = "",
            quantityStr = "0",
            animalsRemaining = 100,
            projectType = ProjectType.POULTRY
        )
        assertEquals("Date must be between the project start date and today.", futureResult.dateError)
    }

    // Verifies that credit sales require a customer name.
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

    // Verifies that selling more animals than remaining returns an exact error message.
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

    // Verifies that a valid sale within remaining stock limits passes.
    @Test
    fun validSale_passes() {
        val result = validateTransaction(
            kind = EntryKind.SALE,
            category = "Sale",
            amountStr = "500.0",
            dateMs = now - 1000,
            projectStartDateMs = start,
            currentDateMs = now,
            paymentType = PaymentType.CREDIT,
            customerName = "Mr Banda",
            quantityStr = "20",
            animalsRemaining = 50,
            projectType = ProjectType.POULTRY
        )
        assertTrue(result.isValid)
        assertNull(result.quantityError)
        assertNull(result.customerNameError)
    }
}
