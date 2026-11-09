package com.example.farmledger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmledger.data.EntryKind
import com.example.farmledger.data.LedgerEntry
import com.example.farmledger.data.PaymentType
import com.example.farmledger.data.ProjectType

// Renders the detailed metrics, reminder alerts, and transaction history for a single project.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: Long,
    onAddTransaction: () -> Unit,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ProjectDetailsViewModel = viewModel(
        key = "details-$projectId",
        factory = AppViewModelProvider.projectDetailsFactory(projectId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val project = uiState.project

    var showDeleteProjectDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<LedgerEntry?>(null) }

    if (showDeleteProjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteProjectDialog = false },
            title = { Text("Delete Project") },
            text = { Text("Delete this project and all its transactions and reminders?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteProjectDialog = false
                    viewModel.deleteProject(onDeleted)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    transactionToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Delete this transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(entry)
                    transactionToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "Project Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteProjectDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Project")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (project != null) {
                val displayCategoryName = if (project.type == ProjectType.GARDEN && project.variety.isNotBlank()) {
                    "Garden – ${project.variety}"
                } else {
                    project.type.toDisplayName()
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Category: $displayCategoryName", style = MaterialTheme.typography.bodyLarge)
                            Text("Started: ${formatDate(project.startDate)}", style = MaterialTheme.typography.bodyMedium)

                            val budget = project.budget
                            val spent = uiState.totalExpenses
                            if (spent > budget) {
                                val over = spent - budget
                                Text(
                                    text = "Budget ${formatCurrency(budget)} · Spent ${formatCurrency(spent)} · Over budget by ${formatCurrency(over)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB3261E)
                                )
                            } else {
                                val left = budget - spent
                                Text(
                                    text = "Budget ${formatCurrency(budget)} · Spent ${formatCurrency(spent)} · Left ${formatCurrency(left)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Project Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expenses: ${formatCurrency(uiState.totalExpenses)}", color = Color(0xFFB3261E))
                                Text("Sales: ${formatCurrency(uiState.totalSales)}", color = Color(0xFF2E6B3A))
                            }

                            val isProfit = uiState.profit >= 0.0
                            val profitColor = if (isProfit) Color(0xFF2E6B3A) else Color(0xFFB3261E)
                            Text(
                                text = profitLabel(uiState.profit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = profitColor
                            )

                            val remainingText = if (uiState.animalsRemaining > 0) {
                                "Running total – ${uiState.animalsRemaining} still to sell"
                            } else {
                                "Final result – all sold"
                            }
                            Text(
                                text = remainingText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                uiState.nextReminder?.let { reminder ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Next Reminder", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                                Text("Due: ${formatDate(reminder.dueDate)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = onAddTransaction,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add Transaction")
                    }
                }

                item {
                    Text("Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        Text("No transactions recorded yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(uiState.transactions, key = { it.id }) { entry ->
                        TransactionRow(
                            entry = entry,
                            customersMap = uiState.customers,
                            onDelete = { transactionToDelete = entry }
                        )
                    }
                }
            }
        }
    }
}

// Displays an individual ledger transaction item with option to delete.
@Composable
private fun TransactionRow(
    entry: LedgerEntry,
    customersMap: Map<Long, String>,
    onDelete: () -> Unit
) {
    val isExpense = entry.kind == EntryKind.EXPENSE
    val amountColor = if (isExpense) Color(0xFFB3261E) else Color(0xFF2E6B3A)
    val displayKind = if (isExpense) "Expense" else "Sale"
    val displayCategory = if (isExpense) {
        entry.category
    } else if (entry.paymentType == PaymentType.CREDIT) {
        val customerName = entry.customerId?.let { customersMap[it] } ?: ""
        if (customerName.isNotBlank()) "Sold ${entry.quantity} · Credit – $customerName" else "Sold ${entry.quantity} · Credit"
    } else {
        "Sold ${entry.quantity}"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = displayKind,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                    Text(displayCategory, style = MaterialTheme.typography.bodyMedium)
                }

                if (entry.note.isNotBlank()) {
                    Text("Note: ${entry.note}", style = MaterialTheme.typography.bodySmall)
                }

                Text(formatDate(entry.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCurrency(entry.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    modifier = Modifier.padding(end = 8.dp)
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
