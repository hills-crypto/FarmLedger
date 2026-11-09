package com.example.farmledger.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmledger.data.FarmRepository
import com.example.farmledger.data.Project
import com.example.farmledger.data.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Holds the UI state for rendering reminders, available projects, and project name lookup maps.
data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val projects: List<Project> = emptyList(),
    val projectNames: Map<Long, String> = emptyMap()
)

// Manages business logic and data operations for viewing, adding, and completing reminders.
class RemindersViewModel(private val repository: FarmRepository) : ViewModel() {

    // Reactive state flow that combines active reminders and active farming project mappings.
    val uiState: StateFlow<RemindersUiState> = combine(
        repository.getUpcomingReminders(),
        repository.getAllProjects()
    ) { reminders, projects ->
        val projectMap = projects.associate { it.id to it.name }
        RemindersUiState(
            reminders = reminders,
            projects = projects,
            projectNames = projectMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RemindersUiState()
    )

    var customReminderError by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    // Clears custom reminder validation error state.
    fun clearError() {
        customReminderError = null
    }

    // Marks a reminder as completed in Room and cancels its scheduled notification job.
    fun markDone(reminder: Reminder) {
        viewModelScope.launch {
            repository.markReminderDone(reminder)
        }
    }

    // Validates and saves a custom user-defined reminder.
    fun addCustomReminder(
        projectId: Long,
        title: String,
        dueDateMs: Long,
        onDone: () -> Unit
    ) {
        if (isSaving) return

        val error = validateReminder(title, dueDateMs, System.currentTimeMillis())
        customReminderError = error
        if (error != null) return

        isSaving = true
        viewModelScope.launch {
            try {
                repository.addCustomReminder(projectId, title, dueDateMs)
                customReminderError = null
                onDone()
            } finally {
                isSaving = false
            }
        }
    }

    // Schedules a test notification to trigger after 15 seconds.
    fun testNotification() {
        repository.scheduleTestNotification()
    }
}
