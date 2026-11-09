package com.example.farmledger.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmledger.data.FarmRepository
import com.example.farmledger.data.Project
import com.example.farmledger.data.ProjectType
import kotlinx.coroutines.launch

// Manages form state and validation logic when creating a new farming project.
class AddProjectViewModel(private val repository: FarmRepository) : ViewModel() {
    var name by mutableStateOf("")
        private set

    var quantity by mutableStateOf("")
        private set

    var budget by mutableStateOf("")
        private set

    var type by mutableStateOf(ProjectType.POULTRY)
        private set

    var startDate by mutableLongStateOf(System.currentTimeMillis())
        private set

    var nameError by mutableStateOf<String?>(null)
        private set

    var quantityError by mutableStateOf<String?>(null)
        private set

    var budgetError by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    // Resets all form fields, errors, and saving state back to default values.
    fun reset() {
        name = ""
        quantity = ""
        budget = ""
        type = ProjectType.POULTRY
        startDate = System.currentTimeMillis()
        nameError = null
        quantityError = null
        budgetError = null
        isSaving = false
    }

    // Updates the input name value and clears its error message.
    fun updateName(input: String) {
        name = input
        nameError = null
    }

    // Updates the input quantity string and clears its error message.
    fun updateQuantity(input: String) {
        quantity = input
        quantityError = null
    }

    // Updates the input budget string and clears its error message.
    fun updateBudget(input: String) {
        budget = input
        budgetError = null
    }

    // Updates the selected project type.
    fun updateType(newType: ProjectType) {
        type = newType
    }

    // Updates the chosen project start date timestamp.
    fun updateStartDate(dateMs: Long) {
        startDate = dateMs
    }

    // Validates inputs and saves the project with generated reminders if valid.
    fun save(onSaved: () -> Unit) {
        if (isSaving) return

        var isValid = true

        if (name.isBlank()) {
            nameError = "Please enter a project name."
            isValid = false
        }

        val parsedQuantity = quantity.toIntOrNull()
        if (parsedQuantity == null || parsedQuantity <= 0) {
            quantityError = "Please enter a valid quantity greater than zero."
            isValid = false
        }

        val parsedBudget = budget.toDoubleOrNull()
        if (parsedBudget == null || parsedBudget <= 0.0) {
            budgetError = "Please enter a valid budget greater than zero."
            isValid = false
        }

        if (!isValid) return

        isSaving = true
        viewModelScope.launch {
            try {
                val project = Project(
                    name = name.trim(),
                    type = type,
                    quantity = parsedQuantity!!,
                    startDate = startDate,
                    budget = parsedBudget!!
                )
                repository.saveProjectWithReminders(project)
                reset()
                onSaved()
            } finally {
                isSaving = false
            }
        }
    }
}
