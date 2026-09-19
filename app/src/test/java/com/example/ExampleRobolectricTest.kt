package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiInsightService
import com.example.data.model.BudgetEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Budget Manager", appName)
  }

  @Test
  fun `test ai smart tips generation`() {
    val aiService = AiInsightService()
    val now = System.currentTimeMillis()
    val txs = listOf(
      TransactionEntity(
        title = "Salary",
        amount = 50000.0,
        type = TransactionType.INCOME.name,
        category = "Income",
        dateMillis = now
      ),
      TransactionEntity(
        title = "Dinner",
        amount = 3500.0,
        type = TransactionType.EXPENSE.name,
        category = "Food & Dining",
        dateMillis = now
      )
    )
    val budgets = listOf(
      BudgetEntity(category = "Food & Dining", monthlyLimit = 3000.0)
    )
    val goals = listOf(
      SavingsGoalEntity(
        title = "Emergency Fund",
        targetAmount = 20000.0,
        currentAmount = 10000.0,
        targetDateMillis = now + 1000000L
      )
    )

    val tips = aiService.generateLocalInsights(txs, budgets, goals)
    assertTrue("Should generate at least one financial tip", tips.isNotEmpty())
  }
}

