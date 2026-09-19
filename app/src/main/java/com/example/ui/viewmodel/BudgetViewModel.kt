package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiInsightService
import com.example.data.ai.SmartFinancialTip
import com.example.data.local.AppDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategorySpending
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateRangeFilter {
    ALL,
    THIS_MONTH,
    LAST_30_DAYS,
    THIS_WEEK
}

enum class SortOption {
    NEWEST,
    OLDEST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT
}

data class DashboardSummary(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val savingsRatePercentage: Int = 0,
    val budgetConsumedPercentage: Int = 0,
    val totalMonthlyBudget: Double = 0.0
)

data class FilterCriteria(
    val query: String = "",
    val typeFilter: String? = "ALL",
    val categoryFilter: String? = "ALL",
    val dateRange: DateRangeFilter = DateRangeFilter.ALL,
    val sortOption: SortOption = SortOption.NEWEST
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository
    private val aiService = AiInsightService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BudgetRepository(
            transactionDao = db.transactionDao(),
            budgetDao = db.budgetDao(),
            savingsGoalDao = db.savingsGoalDao()
        )
        // Seed initial data if empty
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Raw Room Flows
    val allTransactions = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBudgets = repository.allBudgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allGoals = repository.allGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI Search & Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<String?>("ALL")
    val selectedTypeFilter: StateFlow<String?> = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>("ALL")
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedDateRange = MutableStateFlow(DateRangeFilter.ALL)
    val selectedDateRange: StateFlow<DateRangeFilter> = _selectedDateRange.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filterCriteria = MutableStateFlow(FilterCriteria())

    // Dark Mode Override (defaults to null = follow system, can be toggled manually)
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    // AI Advice State
    private val _aiAdviceText = MutableStateFlow<String?>(null)
    val aiAdviceText: StateFlow<String?> = _aiAdviceText.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _filterCriteria
    ) { transactions, criteria ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        transactions.filter { tx ->
            // Search query filter
            val matchesQuery = criteria.query.isBlank() ||
                    tx.title.contains(criteria.query, ignoreCase = true) ||
                    tx.notes.contains(criteria.query, ignoreCase = true) ||
                    tx.category.contains(criteria.query, ignoreCase = true)

            // Type filter
            val matchesType = criteria.typeFilter == "ALL" || criteria.typeFilter == null || tx.type.equals(criteria.typeFilter, ignoreCase = true)

            // Category filter
            val matchesCategory = criteria.categoryFilter == "ALL" || criteria.categoryFilter == null || tx.category.equals(criteria.categoryFilter, ignoreCase = true)

            // Date range filter
            val matchesDate = when (criteria.dateRange) {
                DateRangeFilter.ALL -> true
                DateRangeFilter.THIS_WEEK -> {
                    val oneWeekAgo = now - (7L * 24 * 60 * 60 * 1000)
                    tx.dateMillis >= oneWeekAgo
                }
                DateRangeFilter.LAST_30_DAYS -> {
                    val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                    tx.dateMillis >= thirtyDaysAgo
                }
                DateRangeFilter.THIS_MONTH -> {
                    cal.timeInMillis = now
                    val currentMonth = cal.get(Calendar.MONTH)
                    val currentYear = cal.get(Calendar.YEAR)
                    cal.timeInMillis = tx.dateMillis
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }
            }

            matchesQuery && matchesType && matchesCategory && matchesDate
        }.let { list ->
            when (criteria.sortOption) {
                SortOption.NEWEST -> list.sortedByDescending { it.dateMillis }
                SortOption.OLDEST -> list.sortedBy { it.dateMillis }
                SortOption.HIGHEST_AMOUNT -> list.sortedByDescending { it.amount }
                SortOption.LOWEST_AMOUNT -> list.sortedBy { it.amount }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard Financial Metrics
    val dashboardSummary = combine(allTransactions, allBudgets) { transactions, budgets ->
        val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        val savingsRate = if (totalIncome > 0) {
            (((totalIncome - totalExpense) / totalIncome) * 100).toInt().coerceAtLeast(0)
        } else 0

        val totalBudget = budgets.sumOf { it.monthlyLimit }
        val budgetConsumed = if (totalBudget > 0) {
            ((totalExpense / totalBudget) * 100).toInt()
        } else 0

        DashboardSummary(
            totalBalance = balance,
            monthlyIncome = totalIncome,
            monthlyExpense = totalExpense,
            savingsRatePercentage = savingsRate,
            budgetConsumedPercentage = budgetConsumed,
            totalMonthlyBudget = totalBudget
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary()
    )

    // Category Spending vs Budget List
    val categorySpendings = combine(allTransactions, allBudgets) { transactions, budgets ->
        val expenses = transactions.filter { it.isExpense }
        val spentMap = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }

        val budgetMap = budgets.associateBy({ it.category }, { it.monthlyLimit })

        // Merge all categories present in either budgets or expenses
        val allCategories = (budgetMap.keys + spentMap.keys).distinct().sorted()

        allCategories.map { category ->
            val spent = spentMap[category] ?: 0.0
            val limit = budgetMap[category] ?: 0.0
            val ratio = if (limit > 0) (spent / limit).toFloat() else if (spent > 0) 1.0f else 0f
            CategorySpending(
                category = category,
                amount = spent,
                budgetLimit = limit,
                percentage = ratio,
                isOverBudget = ratio >= 1.0f && limit > 0,
                isNearLimit = ratio >= 0.8f && ratio < 1.0f && limit > 0
            )
        }.sortedByDescending { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Automated Local Smart Tips
    val localSmartTips = combine(allTransactions, allBudgets, allGoals) { transactions, budgets, goals ->
        aiService.generateLocalInsights(transactions, budgets, goals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filter controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _filterCriteria.value = _filterCriteria.value.copy(query = query)
    }

    fun setTypeFilter(type: String?) {
        _selectedTypeFilter.value = type
        _filterCriteria.value = _filterCriteria.value.copy(typeFilter = type)
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
        _filterCriteria.value = _filterCriteria.value.copy(categoryFilter = category)
    }

    fun setDateRange(range: DateRangeFilter) {
        _selectedDateRange.value = range
        _filterCriteria.value = _filterCriteria.value.copy(dateRange = range)
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        _filterCriteria.value = _filterCriteria.value.copy(sortOption = option)
    }

    fun toggleDarkMode(currentSystemDark: Boolean) {
        val current = _isDarkMode.value ?: currentSystemDark
        _isDarkMode.value = !current
    }

    // Transaction Actions
    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        dateMillis: Long,
        notes: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title.ifBlank { category },
                amount = amount,
                type = type.name,
                category = category,
                dateMillis = dateMillis,
                notes = notes
            )
            repository.insertTransaction(entity)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    // Budget Actions
    fun setCategoryBudget(category: String, monthlyLimit: Double) {
        viewModelScope.launch {
            repository.setBudget(BudgetEntity(category, monthlyLimit))
        }
    }

    // Savings Goals Actions
    fun addSavingsGoal(
        title: String,
        targetAmount: Double,
        initialDeposit: Double,
        targetDateMillis: Long,
        icon: String
    ) {
        viewModelScope.launch {
            val goal = SavingsGoalEntity(
                title = title,
                targetAmount = targetAmount,
                currentAmount = initialDeposit,
                targetDateMillis = targetDateMillis,
                categoryIcon = icon,
                isCompleted = initialDeposit >= targetAmount
            )
            repository.insertSavingsGoal(goal)
        }
    }

    fun addFundsToGoal(id: Long, amount: Double) {
        viewModelScope.launch {
            repository.addFundsToGoal(id, amount)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    // AI Advisor Request
    fun requestAiAdvice() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiService.fetchAiFinancialAdvice(
                transactions = allTransactions.value,
                budgets = allBudgets.value,
                goals = allGoals.value
            )
            _isAiLoading.value = false
            _aiAdviceText.value = result.getOrNull()
        }
    }

    // CSV Export Generator
    fun exportTransactionsCsv(context: Context) {
        viewModelScope.launch {
            try {
                val transactions = allTransactions.value
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                val csvHeader = "ID,Date,Type,Category,Title,Amount,Notes\n"
                val csvContent = buildString {
                    append(csvHeader)
                    transactions.forEach { tx ->
                        val cleanTitle = tx.title.replace("\"", "\"\"")
                        val cleanNotes = tx.notes.replace("\"", "\"\"")
                        val cleanCat = tx.category.replace("\"", "\"\"")
                        val dateStr = dateFormat.format(Date(tx.dateMillis))
                        append("${tx.id},\"$dateStr\",${tx.type},\"$cleanCat\",\"$cleanTitle\",${tx.amount},\"$cleanNotes\"\n")
                    }
                }

                // Write to cache file for sharing
                val cacheDir = context.cacheDir
                val exportFile = File(cacheDir, "transactions_export_${System.currentTimeMillis()}.csv")
                FileWriter(exportFile).use { writer ->
                    writer.write(csvContent)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    exportFile
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Budget Manager Transactions Export")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(sendIntent, "Export Transactions CSV")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
