package com.example.farmledger.data

import android.content.Context

// Dependency container interface that provides the central FarmRepository.
interface AppContainer {
    val farmRepository: FarmRepository
}

// Default implementation of AppContainer that initializes the Room database and constructs FarmRepository.
class DefaultAppContainer(private val context: Context) : AppContainer {
    // Lazy initialization of the central FarmRepository using database DAOs.
    override val farmRepository: FarmRepository by lazy {
        val database = FarmDatabase.getDatabase(context)
        FarmRepository(
            projectDao = database.projectDao(),
            ledgerDao = database.ledgerDao(),
            customerDao = database.customerDao(),
            paymentDao = database.paymentDao(),
            reminderDao = database.reminderDao()
        )
    }
}
