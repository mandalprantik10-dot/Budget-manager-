package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.SavingsGoalDao
import com.example.data.local.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun setBudget(budget: BudgetEntity) {
        budgetDao.insertOrUpdateBudget(budget)
    }

    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long {
        return savingsGoalDao.insertGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun addFundsToGoal(id: Long, additionalAmount: Double) {
        val currentGoal = savingsGoalDao.getGoalById(id) ?: return
        val newAmount = currentGoal.currentAmount + additionalAmount
        val isCompleted = newAmount >= currentGoal.targetAmount
        savingsGoalDao.updateProgress(id, newAmount, isCompleted)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteGoal(goal)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (transactionDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            // Seed Budgets
            val defaultBudgets = listOf(
                BudgetEntity("Food & Dining", 12000.0),
                BudgetEntity("Transport", 5000.0),
                BudgetEntity("Shopping", 10000.0),
                BudgetEntity("Bills & Utilities", 6500.0),
                BudgetEntity("Entertainment", 4500.0),
                BudgetEntity("Health", 4000.0)
            )
            budgetDao.insertAll(defaultBudgets)

            // Helper to get past millis
            fun daysAgo(days: Int): Long {
                cal.timeInMillis = now
                cal.add(Calendar.DAY_OF_YEAR, -days)
                return cal.timeInMillis
            }

            // Seed realistic transactions
            val initialTransactions = listOf(
                TransactionEntity(
                    title = "Monthly Salary",
                    amount = 65000.0,
                    type = TransactionType.INCOME.name,
                    category = "Income",
                    dateMillis = daysAgo(17),
                    notes = "Tech company direct deposit"
                ),
                TransactionEntity(
                    title = "Freelance UI Design",
                    amount = 18500.0,
                    type = TransactionType.INCOME.name,
                    category = "Income",
                    dateMillis = daysAgo(6),
                    notes = "Mobile app redesign milestone"
                ),
                TransactionEntity(
                    title = "Organic Grocery Market",
                    amount = 3850.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Food & Dining",
                    dateMillis = daysAgo(1),
                    notes = "Weekly fresh vegetables, milk, pantry"
                ),
                TransactionEntity(
                    title = "Metro Smart Card Recharge",
                    amount = 1200.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Transport",
                    dateMillis = daysAgo(2),
                    notes = "Monthly commute pass"
                ),
                TransactionEntity(
                    title = "Noise-Cancelling Headphones",
                    amount = 4999.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Shopping",
                    dateMillis = daysAgo(4),
                    notes = "Sale offer on wireless headphones"
                ),
                TransactionEntity(
                    title = "High-speed Fiber Internet",
                    amount = 1499.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Bills & Utilities",
                    dateMillis = daysAgo(5),
                    notes = "Monthly Gigabit connection bill"
                ),
                TransactionEntity(
                    title = "Gourmet Dinner & Bistro",
                    amount = 2650.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Food & Dining",
                    dateMillis = daysAgo(7),
                    notes = "Weekend celebration with friends"
                ),
                TransactionEntity(
                    title = "Cinema IMAX & Popcorn",
                    amount = 1100.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Entertainment",
                    dateMillis = daysAgo(8),
                    notes = "Blockbuster premier weekend"
                ),
                TransactionEntity(
                    title = "Pharmacy & Vitamins",
                    amount = 950.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Health",
                    dateMillis = daysAgo(10),
                    notes = "Omega 3 & multivitamin refills"
                ),
                TransactionEntity(
                    title = "Electric Utility Bill",
                    amount = 2300.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Bills & Utilities",
                    dateMillis = daysAgo(12),
                    notes = "Summer electricity bill"
                ),
                TransactionEntity(
                    title = "Cab Rides to Client Meeting",
                    amount = 680.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Transport",
                    dateMillis = daysAgo(14),
                    notes = "Airport express & intra-city ride"
                ),
                TransactionEntity(
                    title = "Sneakers & Gym Apparel",
                    amount = 3200.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Shopping",
                    dateMillis = daysAgo(15),
                    notes = "Sportswear upgrade"
                )
            )
            transactionDao.insertAll(initialTransactions)

            // Seed Savings Goals
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 45)
            val headphoneTargetDate = cal.timeInMillis

            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 180)
            val emergencyTargetDate = cal.timeInMillis

            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 75)
            val vacationTargetDate = cal.timeInMillis

            val initialGoals = listOf(
                SavingsGoalEntity(
                    title = "New Headphones",
                    targetAmount = 5000.0,
                    currentAmount = 3500.0,
                    targetDateMillis = headphoneTargetDate,
                    categoryIcon = "headphones"
                ),
                SavingsGoalEntity(
                    title = "Emergency Fund",
                    targetAmount = 50000.0,
                    currentAmount = 32000.0,
                    targetDateMillis = emergencyTargetDate,
                    categoryIcon = "shield"
                ),
                SavingsGoalEntity(
                    title = "Weekend Getaway",
                    targetAmount = 15000.0,
                    currentAmount = 8500.0,
                    targetDateMillis = vacationTargetDate,
                    categoryIcon = "flight"
                )
            )
            savingsGoalDao.insertAll(initialGoals)
        }
    }
}
