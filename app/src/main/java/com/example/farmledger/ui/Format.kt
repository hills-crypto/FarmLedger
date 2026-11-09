package com.example.farmledger.ui

import com.example.farmledger.data.ProjectType
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

// Converts ProjectType enum to user-friendly title case display name (e.g., "Poultry").
fun ProjectType.toDisplayName(): String {
    return name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
