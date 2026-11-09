package com.example.farmledger.ui

// Pure function that validates title and due date fields when creating a custom reminder.
fun validateReminder(title: String, dueDateMs: Long, todayMs: Long): String? {
    if (title.isBlank()) {
        return "Please enter what needs to be done."
    }
    if (dayIndex(dueDateMs) < dayIndex(todayMs)) {
        return "The date cannot be in the past."
    }
    return null
}
