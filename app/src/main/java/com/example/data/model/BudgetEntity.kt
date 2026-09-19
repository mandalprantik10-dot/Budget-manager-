package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val monthlyLimit: Double,
    val monthYear: String = "" // Empty means default monthly limit, or "2026-09"
)
