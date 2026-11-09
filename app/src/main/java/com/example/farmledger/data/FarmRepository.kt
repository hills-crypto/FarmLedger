package com.example.farmledger.data

import kotlinx.coroutines.flow.Flow

// Single repository class wrapping all DAOs to provide data access for ViewModels.
class FarmRepository(
    private val projectDao: ProjectDao,
    private val ledgerDao: LedgerDao,
    private val customerDao: CustomerDao,
    private val paymentDao: PaymentDao,
    private val reminderDao: ReminderDao
) {
    // Project operations
    // Retrieves all active farm projects ordered by start date.
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAll()

    // Retrieves a specific project by ID.
    fun getProjectById(id: Long): Flow<Project?> = projectDao.getById(id)

    // Inserts a new project into the database.
    suspend fun insertProject(project: Project): Long = projectDao.insert(project)

    // Inserts a new project into the database and automatically generates its standard reminders.
    suspend fun saveProjectWithReminders(project: Project): Long {
        val projectId = projectDao.insert(project)
        val savedProject = project.copy(id = projectId)
        val reminders = ReminderSchedule.remindersFor(savedProject)
        for (reminder in reminders) {
            reminderDao.insert(reminder)
        }
        return projectId
    }

    // Updates an existing project.
    suspend fun updateProject(project: Project) = projectDao.update(project)

    // Deletes a project from the database.
    suspend fun deleteProject(project: Project) = projectDao.delete(project)

    // Ledger transaction operations
    // Retrieves all ledger entries for a project ordered newest first.
    fun getTransactionsForProject(projectId: Long): Flow<List<LedgerEntry>> = ledgerDao.getForProject(projectId)

    // Calculates total expenses for a project.
    fun getTotalExpenses(projectId: Long): Flow<Double> = ledgerDao.totalExpenses(projectId)

    // Calculates total sales revenue for a project.
    fun getTotalSales(projectId: Long): Flow<Double> = ledgerDao.totalSales(projectId)

    // Calculates total quantity sold for a project.
    fun getQuantitySold(projectId: Long): Flow<Int> = ledgerDao.quantitySold(projectId)

    // Calculates total net profit across all projects.
    fun getTotalProfitAllProjects(): Flow<Double> = ledgerDao.totalProfitAllProjects()

    // Inserts a new transaction entry into the database.
    suspend fun insertTransaction(entry: LedgerEntry): Long = ledgerDao.insert(entry)

    // Updates a transaction entry.
    suspend fun updateTransaction(entry: LedgerEntry) = ledgerDao.update(entry)

    // Deletes a transaction entry.
    suspend fun deleteTransaction(entry: LedgerEntry) = ledgerDao.delete(entry)

    // Customer & debt operations
    // Inserts a customer into the database.
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insert(customer)

    // Finds a customer by name if they already exist.
    suspend fun findCustomerByName(name: String): Customer? = customerDao.findByName(name)

    // Retrieves a customer by ID.
    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getById(id)

    // Retrieves customers who currently have an outstanding credit balance.
    fun getCustomersWithBalance(): Flow<List<CustomerBalance>> = customerDao.customersWithBalance()

    // Calculates total money currently owed across all customers.
    fun getTotalOwed(): Flow<Double> = customerDao.totalOwed()

    // Payment operations
    // Records a debt payment for a customer.
    suspend fun insertPayment(payment: Payment): Long = paymentDao.insert(payment)

    // Retrieves payment history for a customer.
    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>> = paymentDao.getForCustomer(customerId)

    // Reminder operations
    // Inserts a reminder and returns its database ID.
    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insert(reminder)

    // Updates an existing reminder.
    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)

    // Retrieves all pending upcoming reminders.
    fun getUpcomingReminders(): Flow<List<Reminder>> = reminderDao.getUpcoming()

    // Retrieves all reminders linked to a project.
    fun getRemindersForProject(projectId: Long): Flow<List<Reminder>> = reminderDao.getForProject(projectId)

    // Retrieves the next 3 upcoming reminders for dashboard summary.
    fun getNextThreeReminders(): Flow<List<Reminder>> = reminderDao.getNextThree()
}
