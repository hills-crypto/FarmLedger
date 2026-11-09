package com.example.farmledger.data

// Categorizes the type of farm project such as poultry, goats, or garden crops.
enum class ProjectType {
    POULTRY,
    GOATS,
    GARDEN
}

// Distinguishes whether a ledger transaction is an expense or a sale.
enum class EntryKind {
    EXPENSE,
    SALE
}

// Indicates whether a sale was paid immediately in cash or sold on credit.
enum class PaymentType {
    CASH,
    CREDIT
}
