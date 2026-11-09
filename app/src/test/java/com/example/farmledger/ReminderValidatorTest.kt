package com.example.farmledger

import com.example.farmledger.ui.validateReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Unit test suite verifying input validation rules for custom reminders.
class ReminderValidatorTest {

    private val now = System.currentTimeMillis()

    // Verifies that a valid title and future/today due date pass validation.
    @Test
    fun validReminder_passes() {
        val result = validateReminder(
            title = "Deworming check",
            dueDateMs = now + (2 * 24 * 60 * 60 * 1000L),
            todayMs = now
        )
        assertNull(result)
    }

    // Verifies that a blank reminder title produces an error.
    @Test
    fun blankTitle_fails() {
        val result = validateReminder(
            title = "   ",
            dueDateMs = now + (2 * 24 * 60 * 60 * 1000L),
            todayMs = now
        )
        assertEquals("Please enter what needs to be done.", result)
    }

    // Verifies that a due date in the past fails validation.
    @Test
    fun pastDueDate_fails() {
        val pastDate = now - (2 * 24 * 60 * 60 * 1000L) // 2 days ago
        val result = validateReminder(
            title = "Deworming check",
            dueDateMs = pastDate,
            todayMs = now
        )
        assertEquals("The date cannot be in the past.", result)
    }
}
