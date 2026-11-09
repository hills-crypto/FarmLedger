package com.example.farmledger

import com.example.farmledger.ui.profitLabel
import org.junit.Assert.assertEquals
import org.junit.Test

// Unit test suite verifying profit and loss string label formatting.
class FormatTest {

    // Verifies positive net profit formatting.
    @Test
    fun positiveProfit_formatsCorrectly() {
        val label = profitLabel(59000.0)
        assertEquals("Profit: K59,000.00", label)
    }

    // Verifies zero profit formatting.
    @Test
    fun zeroProfit_formatsCorrectly() {
        val label = profitLabel(0.0)
        assertEquals("Profit: K0.00", label)
    }

    // Verifies negative loss formatting without negative sign.
    @Test
    fun negativeLoss_formatsCorrectly() {
        val label = profitLabel(-3000.0)
        assertEquals("Loss: K3,000.00", label)
    }
}
