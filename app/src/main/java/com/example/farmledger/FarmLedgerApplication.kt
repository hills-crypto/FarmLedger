package com.example.farmledger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.farmledger.data.AppContainer
import com.example.farmledger.data.DefaultAppContainer

// Application class that initializes the dependency container and configures notification channels.
class FarmLedgerApplication : Application() {
    // Container instance accessed by ViewModels and Composables to get the repository.
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        createNotificationChannel()
    }

    // Creates the default system notification channel for scheduling farm reminder alerts.
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminders",
                "Farm reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming farm vaccination, feeding, and harvest reminders."
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
