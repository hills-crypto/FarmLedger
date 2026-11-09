package com.example.farmledger.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formats a monetary amount into standard Zambian Kwacha string representation with comma separators.
fun formatCurrency(amount: Double): String {
    return String.format(Locale.US, "K%,.2f", amount)
}

// Formats a epoch millisecond timestamp into a human-readable date string.
fun formatDate(epochMs: Long): String {
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return formatter.format(Date(epochMs))
}
