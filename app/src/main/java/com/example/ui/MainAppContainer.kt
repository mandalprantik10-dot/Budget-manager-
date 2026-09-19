package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryUtils
import com.example.ui.components.TransactionDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch

enum class AppDestination(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    BUDGETS("Budgets", Icons.Default.PieChart),
    GOALS("Goals", Icons.Default.Savings),
    TRANSACTIONS("History", Icons.Default.ReceiptLong)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dark Mode handling
    val systemDark = isSystemInDarkTheme()
    val isDarkOverride by viewModel.isDarkMode.collectAsState()
    val useDarkMode = isDarkOverride ?: systemDark

    // ViewModel states
    val summary by viewModel.dashboardSummary.collectAsState()
    val categorySpendings by viewModel.categorySpendings.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val savingsGoals by viewModel.allGoals.collectAsState()
    val smartTips by viewModel.localSmartTips.collectAsState()
    val aiAdviceText by viewModel.aiAdviceText.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    // Navigation & Modal Dialog states
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    MyApplicationTheme(darkTheme = useDarkMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp

            Scaffold(
                modifier = modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Budget Manager",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        actions = {
                            // Dark / Light Mode Toggle
                            IconButton(
                                onClick = { viewModel.toggleDarkMode(systemDark) },
                                modifier = Modifier.testTag("toggle_dark_mode_button")
                            ) {
                                Icon(
                                    imageVector = if (useDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Dark Mode",
                                    tint = if (useDarkMode) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // CSV Export
                            IconButton(
                                onClick = {
                                    viewModel.exportTransactionsCsv(context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Exporting transaction records...")
                                    }
                                },
                                modifier = Modifier.testTag("top_bar_export_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Export CSV",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            AppDestination.entries.forEach { destination ->
                                val isSelected = currentDestination == destination
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = destination.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = EmeraldPrimary,
                                        selectedTextColor = EmeraldPrimary,
                                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Navigation Rail on tablet / wide screen
                    if (isExpandedScreen) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FloatingActionButton(
                                onClick = {
                                    transactionToEdit = null
                                    showTransactionDialog = true
                                },
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("rail_fab_add")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            AppDestination.entries.forEach { destination ->
                                val isSelected = currentDestination == destination
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.title
                                        )
                                    },
                                    label = { Text(destination.title) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = EmeraldPrimary,
                                        selectedTextColor = EmeraldPrimary,
                                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("rail_${destination.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Main Active Screen with smooth transition
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "MainContentTransition"
                        ) { targetDestination ->
                            when (targetDestination) {
                                AppDestination.DASHBOARD -> DashboardScreen(
                                    summary = summary,
                                    categorySpendings = categorySpendings,
                                    recentTransactions = allTransactions,
                                    savingsGoals = savingsGoals,
                                    smartTips = smartTips,
                                    aiAdviceText = aiAdviceText,
                                    isAiLoading = isAiLoading,
                                    onRequestAiAdvice = { viewModel.requestAiAdvice() },
                                    onAddTransactionClick = {
                                        transactionToEdit = null
                                        showTransactionDialog = true
                                    },
                                    onViewAllTransactions = { currentDestination = AppDestination.TRANSACTIONS },
                                    onViewAnalytics = { currentDestination = AppDestination.ANALYTICS },
                                    onViewBudgets = { currentDestination = AppDestination.BUDGETS },
                                    onTransactionClick = { tx ->
                                        transactionToEdit = tx
                                        showTransactionDialog = true
                                    }
                                )

                                AppDestination.ANALYTICS -> AnalyticsScreen(
                                    summary = summary,
                                    categorySpendings = categorySpendings,
                                    transactions = allTransactions
                                )

                                AppDestination.BUDGETS -> BudgetsScreen(
                                    summary = summary,
                                    categorySpendings = categorySpendings,
                                    onSetBudget = { cat, limit ->
                                        viewModel.setCategoryBudget(cat, limit)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Budget set for $cat: ${CategoryUtils.formatCurrency(limit)}")
                                        }
                                    }
                                )

                                AppDestination.GOALS -> SavingsGoalsScreen(
                                    goals = savingsGoals,
                                    onAddGoal = { title, target, initial, targetDate, icon ->
                                        viewModel.addSavingsGoal(title, target, initial, targetDate, icon)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Created goal: $title")
                                        }
                                    },
                                    onAddFunds = { goalId, amount ->
                                        viewModel.addFundsToGoal(goalId, amount)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Deposited ${CategoryUtils.formatCurrency(amount)} into goal")
                                        }
                                    },
                                    onDeleteGoal = { goal ->
                                        viewModel.deleteSavingsGoal(goal)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Deleted goal: ${goal.title}")
                                        }
                                    }
                                )

                                AppDestination.TRANSACTIONS -> TransactionsScreen(
                                    transactions = filteredTransactions,
                                    searchQuery = searchQuery,
                                    selectedTypeFilter = selectedTypeFilter,
                                    selectedCategoryFilter = selectedCategoryFilter,
                                    selectedDateRange = selectedDateRange,
                                    sortOption = sortOption,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onTypeFilterChange = { viewModel.setTypeFilter(it) },
                                    onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                                    onDateRangeChange = { viewModel.setDateRange(it) },
                                    onSortChange = { viewModel.setSortOption(it) },
                                    onExportCsv = { viewModel.exportTransactionsCsv(context) },
                                    onAddTransaction = {
                                        transactionToEdit = null
                                        showTransactionDialog = true
                                    },
                                    onTransactionClick = { tx ->
                                        transactionToEdit = tx
                                        showTransactionDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Quick Add / Edit Transaction Modal Dialog
            if (showTransactionDialog) {
                TransactionDialog(
                    transactionToEdit = transactionToEdit,
                    onDismiss = {
                        showTransactionDialog = false
                        transactionToEdit = null
                    },
                    onSave = { title, amount, type, category, dateMillis, notes ->
                        if (transactionToEdit != null) {
                            val updated = transactionToEdit!!.copy(
                                title = title,
                                amount = amount,
                                type = type.name,
                                category = category,
                                dateMillis = dateMillis,
                                notes = notes
                            )
                            viewModel.updateTransaction(updated)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Transaction updated")
                            }
                        } else {
                            viewModel.addTransaction(title, amount, type, category, dateMillis, notes)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Transaction added")
                            }
                        }
                        showTransactionDialog = false
                        transactionToEdit = null
                    },
                    onDelete = { tx ->
                        viewModel.deleteTransaction(tx)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Transaction deleted")
                        }
                        showTransactionDialog = false
                        transactionToEdit = null
                    }
                )
            }
        }
    }
}
