package com.example.farmledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// The main local Room database for storing all FarmLedger app data.
@Database(
    entities = [Project::class, LedgerEntry::class, Customer::class, Payment::class, Reminder::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FarmDatabase : RoomDatabase() {
    // Provides access to project database operations.
    abstract fun projectDao(): ProjectDao

    // Provides access to ledger transaction database operations.
    abstract fun ledgerDao(): LedgerDao

    // Provides access to customer database operations.
    abstract fun customerDao(): CustomerDao

    // Provides access to payment database operations.
    abstract fun paymentDao(): PaymentDao

    // Provides access to reminder database operations.
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var Instance: FarmDatabase? = null

        // Returns the single database instance, creating it if it does not yet exist.
        fun getDatabase(context: Context): FarmDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FarmDatabase::class.java,
                    "farm_ledger_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
