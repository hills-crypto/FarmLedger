package com.example.farmledger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmledger.data.FarmRepository
import com.example.farmledger.data.LedgerEntry
import com.example.farmledger.data.Project
import com.example.farmledger.data.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Holds the complete state needed to render the project details overview and transaction list.
data class ProjectDetailsUiState(
    val project: Project? = null,
    val totalExpenses: Double = 0.0,
    val totalSales: Double = 0.0,
    val profit: Double = 0.0,
    val animalsRemaining: Int = 0,
    val transactions: List<LedgerEntry> = emptyList(),
    val nextReminder: Reminder? = null
)

// Manages business logic, calculations, and state streams for a specific project details screen.
class ProjectDetailsViewModel(
    private val repository: FarmRepository,
    private val projectId: Long
) : ViewModel() {

    // Reactive state flow that combines project details, financial totals, remaining counts, and reminders.
    val uiState: StateFlow<ProjectDetailsUiState> = combine(
        combine(
            repository.getProjectById(projectId),
            repository.getTotalExpenses(projectId),
            repository.getTotalSales(projectId)
        ) { project, totalExpenses, totalSales ->
            Triple(project, totalExpenses, totalSales)
        },
        combine(
            repository.getQuantitySold(projectId),
            repository.getTransactionsForProject(projectId),
            repository.getRemindersForProject(projectId)
        ) { quantitySold, transactions, reminders ->
            Triple(quantitySold, transactions, reminders)
        }
    ) { (project, totalExpenses, totalSales), (quantitySold, transactions, reminders) ->
        val profit = totalSales - totalExpenses
        val initialQuantity = project?.quantity ?: 0
        val remaining = (initialQuantity - quantitySold).coerceAtLeast(0)
        val nextReminder = reminders.firstOrNull { !it.isDone }

        ProjectDetailsUiState(
            project = project,
            totalExpenses = totalExpenses,
            totalSales = totalSales,
            profit = profit,
            animalsRemaining = remaining,
            transactions = transactions,
            nextReminder = nextReminder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProjectDetailsUiState()
    )

    // Deletes the project and all cascaded entries from the database.
    fun deleteProject(onDeleted: () -> Unit) {
        viewModelScope.launch {
            uiState.value.project?.let { repository.deleteProject(it) }
            onDeleted()
        }
    }

    // Deletes an individual transaction entry from the project ledger.
    fun deleteTransaction(entry: LedgerEntry) {
        viewModelScope.launch {
            repository.deleteTransaction(entry)
        }
    }
}
