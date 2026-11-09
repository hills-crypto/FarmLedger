package com.example.farmledger

import android.app.Application
import com.example.farmledger.data.AppContainer
import com.example.farmledger.data.DefaultAppContainer

// Application class that initializes and holds the app's central AppContainer dependency container.
class FarmLedgerApplication : Application() {
    // Container instance accessed by ViewModels and Composables to get the repository.
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
