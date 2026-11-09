package com.example.farmledger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmledger.data.FarmRepository
import com.example.farmledger.data.Project
import com.example.farmledger.data.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Holds composite state required to display the main dashboard dashboard screen.
data class DashboardUiState(
    val projects: List<Project> = emptyList(),
    val projectCount: Int = 0,
    val totalProfit: Double = 0.0,
    val totalOwed: Double = 0.0,
    val nextReminders: List<Reminder> = emptyList()
)

// Provides reactive state streams for the main dashboard overview screen.
class DashboardViewModel(repository: FarmRepository) : ViewModel() {
    // Combined state stream that merges projects, profit, customer debts, and upcoming reminders.
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllProjects(),
        repository.getTotalProfitAllProjects(),
        repository.getTotalOwed(),
        repository.getNextThreeReminders()
    ) { projects, totalProfit, totalOwed, nextReminders ->
        DashboardUiState(
            projects = projects,
            projectCount = projects.size,
            totalProfit = totalProfit,
            totalOwed = totalOwed,
            nextReminders = nextReminders
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
