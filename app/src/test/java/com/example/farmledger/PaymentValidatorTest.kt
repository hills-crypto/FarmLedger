package com.example.farmledger

import com.example.farmledger.ui.validatePayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Unit test suite verifying input validation for customer debt payments.
class PaymentValidatorTest {

    // Verifies that a valid payment amount less than or equal to current balance passes.
    @Test
    fun validPayment_passes() {
        val result = validatePayment(amountStr = "1500.0", balance = 2500.0)
        assertNull(result)
    }

    // Verifies that an invalid or zero payment amount returns an error.
    @Test
    fun zeroOrNegativeAmount_fails() {
        val resultZero = validatePayment(amountStr = "0.0", balance = 2500.0)
        assertEquals("Please enter a valid amount greater than zero.", resultZero)

        val resultInvalid = validatePayment(amountStr = "abc", balance = 2500.0)
        assertEquals("Please enter a valid amount greater than zero.", resultInvalid)
    }

    // Verifies that a payment exceeding the balance fails validation.
    @Test
    fun paymentExceedingBalance_fails() {
        val result = validatePayment(amountStr = "3000.0", balance = 2500.0)
        assertEquals("Payment is more than the balance of K2,500.00.", result)
    }
}
