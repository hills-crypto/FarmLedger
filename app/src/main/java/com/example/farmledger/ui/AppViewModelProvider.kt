package com.example.farmledger.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmledger.FarmLedgerApplication

// Provides factory instances for instantiating ViewModels with repository dependencies.
object AppViewModelProvider {
    // Factory that retrieves FarmRepository from the application container to construct ViewModels.
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AddProjectViewModel(farmLedgerApplication().container.farmRepository)
        }
        initializer {
            DashboardViewModel(farmLedgerApplication().container.farmRepository)
        }
    }
}

// Extension function to safely extract the FarmLedgerApplication instance from CreationExtras.
fun CreationExtras.farmLedgerApplication(): FarmLedgerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FarmLedgerApplication)
