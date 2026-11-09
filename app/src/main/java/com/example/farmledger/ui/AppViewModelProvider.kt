package com.example.farmledger.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmledger.FarmLedgerApplication

// Provides factory instances for instantiating ViewModels with repository dependencies.
object AppViewModelProvider {
    // Factory for view models that do not require navigation arguments.
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AddProjectViewModel(farmLedgerApplication().container.farmRepository)
        }
        initializer {
            DashboardViewModel(farmLedgerApplication().container.farmRepository)
        }
        initializer {
            CustomersViewModel(farmLedgerApplication().container.farmRepository)
        }
    }

    // Returns a ViewModelProvider.Factory for ProjectDetailsViewModel parameterized by projectId.
    fun projectDetailsFactory(projectId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProjectDetailsViewModel(
                repository = farmLedgerApplication().container.farmRepository,
                projectId = projectId
            )
        }
    }

    // Returns a ViewModelProvider.Factory for AddTransactionViewModel parameterized by projectId.
    fun addTransactionFactory(projectId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AddTransactionViewModel(
                repository = farmLedgerApplication().container.farmRepository,
                projectId = projectId
            )
        }
    }
}

// Extension function to safely extract the FarmLedgerApplication instance from CreationExtras.
fun CreationExtras.farmLedgerApplication(): FarmLedgerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FarmLedgerApplication)
