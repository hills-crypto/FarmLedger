package com.example.farmledger.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Represents a farming project batch stored in the database.
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: ProjectType,
    val quantity: Int,
    val startDate: Long,
    val budget: Double,
    val variety: String = ""
)

// Represents an individual income or expense entry recorded for a project.
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("customerId")]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val kind: EntryKind,
    val category: String,
    val amount: Double,
    val quantity: Int,
    val date: Long,
    val note: String,
    val paymentType: PaymentType,
    val customerId: Long? = null
)

// Represents a customer who buys farm items on cash or credit.
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String
)

// Represents a cash payment made by a customer to reduce their debt balance.
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val date: Long
)

// Represents a scheduled task or notification reminder linked to a project.
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val dueDate: Long,
    val isDone: Boolean = false,
    val isCustom: Boolean = false
)
