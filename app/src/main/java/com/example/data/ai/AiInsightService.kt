package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.BudgetEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class SmartFinancialTip(
    val title: String,
    val description: String,
    val type: TipType,
    val actionableSuggestion: String? = null
)

enum class TipType {
    WARNING,
    SAVINGS_OPPORTUNITY,
    TREND_ALERT,
    POSITIVE_REINFORCEMENT
}

class AiInsightService {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Generates automated algorithmic insights based on real user spending patterns.
     */
    fun generateLocalInsights(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        goals: List<SavingsGoalEntity>
    ): List<SmartFinancialTip> {
        val tips = mutableListOf<SmartFinancialTip>()
        val now = System.currentTimeMillis()
        val oneWeekMillis = 7L * 24 * 60 * 60 * 1000
        val thisWeekStart = now - oneWeekMillis
        val lastWeekStart = now - (2 * oneWeekMillis)

        val expenses = transactions.filter { it.isExpense }
        val incomes = transactions.filter { it.isIncome }

        val totalIncome = incomes.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }

        // 1. Week-over-week Category Analysis
        val thisWeekExpenses = expenses.filter { it.dateMillis >= thisWeekStart }
        val lastWeekExpenses = expenses.filter { it.dateMillis in lastWeekStart until thisWeekStart }

        val thisWeekByCategory = thisWeekExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        val lastWeekByCategory = lastWeekExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        var foundSurge = false
        for ((category, thisAmount) in thisWeekByCategory) {
            val lastAmount = lastWeekByCategory[category] ?: 0.0
            if (lastAmount > 0) {
                val percentageChange = ((thisAmount - lastAmount) / lastAmount) * 100
                if (percentageChange >= 25) {
                    tips.add(
                        SmartFinancialTip(
                            title = "High Spending on $category",
                            description = "You are spending ${percentageChange.toInt()}% more on $category this week (₹${thisAmount.toInt()}) compared to last week (₹${lastAmount.toInt()}).",
                            type = TipType.TREND_ALERT,
                            actionableSuggestion = "Review recent $category purchases to trim non-essential items."
                        )
                    )
                    foundSurge = true
                    break
                }
            } else if (thisAmount > 2000) {
                tips.add(
                    SmartFinancialTip(
                        title = "New Spike in $category",
                        description = "You've spent ₹${thisAmount.toInt()} on $category this week with no prior transactions in that category last week.",
                        type = TipType.TREND_ALERT,
                        actionableSuggestion = "Keep an eye on this category to avoid unexpected monthly leaks."
                    )
                )
                foundSurge = true
                break
            }
        }

        // 2. Category Budget Warnings (>80% or >100%)
        val categoryExpensesTotal = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { item -> item.amount } }

        var budgetWarningFound = false
        for (budget in budgets) {
            val spent = categoryExpensesTotal[budget.category] ?: 0.0
            val ratio = if (budget.monthlyLimit > 0) spent / budget.monthlyLimit else 0.0

            if (ratio >= 1.0) {
                val exceededBy = spent - budget.monthlyLimit
                tips.add(
                    SmartFinancialTip(
                        title = "Budget Exceeded: ${budget.category}",
                        description = "You have exceeded your monthly limit for ${budget.category} by ₹${exceededBy.toInt()} (${(ratio * 100).toInt()}% consumed).",
                        type = TipType.WARNING,
                        actionableSuggestion = "Pause discretionary spending on ${budget.category} until next month."
                    )
                )
                budgetWarningFound = true
                break
            } else if (ratio >= 0.80) {
                val remaining = budget.monthlyLimit - spent
                tips.add(
                    SmartFinancialTip(
                        title = "Budget Alert: ${budget.category} near limit",
                        description = "You've used ${(ratio * 100).toInt()}% of your ₹${budget.monthlyLimit.toInt()} limit. Only ₹${remaining.toInt()} remaining.",
                        type = TipType.WARNING,
                        actionableSuggestion = "Slow down purchases in ${budget.category} to stay within your goal."
                    )
                )
                budgetWarningFound = true
                break
            }
        }

        // 3. Savings Rate and Goals Advice
        if (totalIncome > 0) {
            val savingsRate = ((totalIncome - totalExpense) / totalIncome) * 100
            if (savingsRate >= 20) {
                val activeGoal = goals.firstOrNull { !it.isCompleted }
                val suggestion = if (activeGoal != null) {
                    val remaining = activeGoal.targetAmount - activeGoal.currentAmount
                    "Allocating ₹${(totalIncome * 0.05).toInt()} to '${activeGoal.title}' will fast-track your goal."
                } else {
                    "Consider setting a new emergency or investment goal to grow your surplus."
                }

                tips.add(
                    SmartFinancialTip(
                        title = "Healthy Savings Rate: ${savingsRate.toInt()}%",
                        description = "Your net cashflow is positive! You have saved ₹${(totalIncome - totalExpense).toInt()} this month.",
                        type = TipType.POSITIVE_REINFORCEMENT,
                        actionableSuggestion = suggestion
                    )
                )
            } else if (savingsRate < 10 && savingsRate >= 0) {
                tips.add(
                    SmartFinancialTip(
                        title = "Tight Monthly Margin",
                        description = "Expenses account for ${(100 - savingsRate).toInt()}% of your income. Your safety buffer is under 10%.",
                        type = TipType.SAVINGS_OPPORTUNITY,
                        actionableSuggestion = "Look into food delivery and subscriptions to free up at least 15% monthly savings."
                    )
                )
            }
        }

        // Default tip if list is sparse
        if (tips.isEmpty()) {
            tips.add(
                SmartFinancialTip(
                    title = "Financial Tip of the Week",
                    description = "Tracking every transaction, even small coffees, saves on average 15-20% each month by eliminating forgotten micro-expenses.",
                    type = TipType.SAVINGS_OPPORTUNITY,
                    actionableSuggestion = "Set a daily spending cap to build disciplined saving momentum."
                )
            )
        }

        return tips
    }

    /**
     * Calls Gemini AI REST API to generate personalized financial advisor insights.
     */
    suspend fun fetchAiFinancialAdvice(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        goals: List<SavingsGoalEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Graceful fallback to rich local analysis
            val localTips = generateLocalInsights(transactions, budgets, goals)
            val combined = localTips.joinToString("\n\n") { "• ${it.title}: ${it.description} ${it.actionableSuggestion ?: ""}" }
            return@withContext Result.success(combined)
        }

        try {
            val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
            val categoryBreakdown = transactions.filter { it.isExpense }
                .groupBy { it.category }
                .map { "${it.key}: ₹${it.value.sumOf { tx -> tx.amount }}" }
                .joinToString(", ")

            val budgetSummary = budgets.map { "${it.category} limit: ₹${it.monthlyLimit}" }.joinToString(", ")
            val goalsSummary = goals.map { "${it.title}: ₹${it.currentAmount}/₹${it.targetAmount}" }.joinToString(", ")

            val prompt = """
                You are an expert personal financial advisor. Analyze this user's monthly finances concisely:
                - Total Monthly Income: ₹$totalIncome
                - Total Monthly Expense: ₹$totalExpense
                - Category Expenses: $categoryBreakdown
                - Monthly Budgets: $budgetSummary
                - Savings Goals: $goalsSummary

                Provide 3 clear, punchy, actionable insights:
                1. A specific habit or category to watch out for.
                2. A concrete savings strategy to reach one of their goals faster.
                3. An encouraging financial milestone or rule of thumb.
                Keep the tone professional, friendly, and under 120 words. Format with bullet points.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                // Fallback to local
                val localTips = generateLocalInsights(transactions, budgets, goals)
                return@withContext Result.success(
                    localTips.joinToString("\n\n") { "• ${it.title}: ${it.description} ${it.actionableSuggestion ?: ""}" }
                )
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                val localTips = generateLocalInsights(transactions, budgets, goals)
                Result.success(localTips.joinToString("\n\n") { "• ${it.title}: ${it.description}" })
            }
        } catch (e: Exception) {
            val localTips = generateLocalInsights(transactions, budgets, goals)
            Result.success(localTips.joinToString("\n\n") { "• ${it.title}: ${it.description} ${it.actionableSuggestion ?: ""}" })
        }
    }
}
