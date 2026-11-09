package com.example.farmledger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Holds information about a customer's total unpaid debt balance and the oldest unpaid credit date.
data class CustomerBalance(
    val id: Long,
    val name: String,
    val phone: String,
    val balance: Double,
    val oldestUnpaidDate: Long?
)

// Data access object for performing CRUD operations on farm projects.
@Dao
interface ProjectDao {
    // Inserts a new project into the database and returns its assigned ID.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    // Updates an existing project's details in the database.
    @Update
    suspend fun update(project: Project)

    // Deletes a project from the database.
    @Delete
    suspend fun delete(project: Project)

    // Retrieves all farming projects ordered from newest start date first.
    @Query("SELECT * FROM projects ORDER BY startDate DESC")
    fun getAll(): Flow<List<Project>>

    // Retrieves a single project by its primary key ID.
    @Query("SELECT * FROM projects WHERE id = :id")
    fun getById(id: Long): Flow<Project?>
}

// Data access object for querying and modifying project ledger transactions.
@Dao
interface LedgerDao {
    // Inserts a new ledger entry into the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LedgerEntry): Long

    // Updates an existing ledger entry in the database.
    @Update
    suspend fun update(entry: LedgerEntry)

    // Deletes a ledger entry from the database.
    @Delete
    suspend fun delete(entry: LedgerEntry)

    // Retrieves all transactions for a specific project sorted newest first.
    @Query("SELECT * FROM transactions WHERE projectId = :projectId ORDER BY date DESC")
    fun getForProject(projectId: Long): Flow<List<LedgerEntry>>

    // Calculates total expenses recorded for a specific project.
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE projectId = :projectId AND kind = 'EXPENSE'")
    fun totalExpenses(projectId: Long): Flow<Double>

    // Calculates total sales revenue recorded for a specific project.
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE projectId = :projectId AND kind = 'SALE'")
    fun totalSales(projectId: Long): Flow<Double>

    // Calculates total animals or items sold for a specific project.
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE projectId = :projectId AND kind = 'SALE'")
    fun quantitySold(projectId: Long): Flow<Int>

    // Calculates overall net profit across all farm projects.
    @Query("SELECT COALESCE(SUM(CASE WHEN kind = 'SALE' THEN amount ELSE -amount END), 0.0) FROM transactions")
    fun totalProfitAllProjects(): Flow<Double>
}

// Data access object for managing customers and calculating unpaid balances.
@Dao
interface CustomerDao {
    // Inserts a new customer into the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    // Searches for an existing customer by case-insensitive name match.
    @Query("SELECT * FROM customers WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): Customer?

    // Retrieves a customer by their unique ID.
    @Query("SELECT * FROM customers WHERE id = :id")
    fun getById(id: Long): Flow<Customer?>

    // Retrieves all customers stored in the database.
    @Query("SELECT * FROM customers")
    fun getAll(): Flow<List<Customer>>

    // Returns a list of customers who currently owe money, calculating credit sales minus payments.
    @Query("""
        SELECT 
            c.id AS id, 
            c.name AS name, 
            c.phone AS phone,
            (COALESCE((SELECT SUM(amount) FROM transactions WHERE customerId = c.id AND kind = 'SALE' AND paymentType = 'CREDIT'), 0.0) - 
             COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id), 0.0)) AS balance,
            (SELECT MIN(date) FROM transactions WHERE customerId = c.id AND kind = 'SALE' AND paymentType = 'CREDIT') AS oldestUnpaidDate
        FROM customers c
        WHERE (COALESCE((SELECT SUM(amount) FROM transactions WHERE customerId = c.id AND kind = 'SALE' AND paymentType = 'CREDIT'), 0.0) - 
               COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id), 0.0)) > 0
    """)
    fun customersWithBalance(): Flow<List<CustomerBalance>>

    // Calculates the total amount of money owed by all customers combined.
    @Query("""
        SELECT COALESCE(SUM(balance), 0.0) FROM (
            SELECT (COALESCE((SELECT SUM(amount) FROM transactions WHERE customerId = c.id AND kind = 'SALE' AND paymentType = 'CREDIT'), 0.0) - 
                    COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id), 0.0)) AS balance
            FROM customers c
        ) WHERE balance > 0
    """)
    fun totalOwed(): Flow<Double>
}

// Data access object for recording customer debt payments.
@Dao
interface PaymentDao {
    // Records a debt payment made by a customer.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    // Retrieves all payments made by a specific customer ordered newest first.
    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getForCustomer(customerId: Long): Flow<List<Payment>>
}

// Data access object for managing animal health and vaccination reminders.
@Dao
interface ReminderDao {
    // Inserts a new reminder into the database and returns its assigned ID.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    // Updates a reminder's status or details.
    @Update
    suspend fun update(reminder: Reminder)

    // Retrieves all upcoming pending reminders sorted by due date.
    @Query("SELECT * FROM reminders WHERE isDone = 0 ORDER BY dueDate ASC")
    fun getUpcoming(): Flow<List<Reminder>>

    // Retrieves all reminders linked to a specific project.
    @Query("SELECT * FROM reminders WHERE projectId = :projectId ORDER BY dueDate ASC")
    fun getForProject(projectId: Long): Flow<List<Reminder>>

    // One-shot fetch of project reminders used prior to project deletion.
    @Query("SELECT * FROM reminders WHERE projectId = :projectId")
    suspend fun getForProjectOnce(projectId: Long): List<Reminder>

    // Retrieves the next three upcoming reminders for the dashboard summary.
    @Query("SELECT * FROM reminders WHERE isDone = 0 ORDER BY dueDate ASC LIMIT 3")
    fun getNextThree(): Flow<List<Reminder>>
}
