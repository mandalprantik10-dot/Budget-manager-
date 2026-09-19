package com.example.data.model

data class CategorySpending(
    val category: String,
    val amount: Double,
    val budgetLimit: Double,
    val percentage: Float, // 0.0 to 1.0+
    val isOverBudget: Boolean,
    val isNearLimit: Boolean // >= 80%
)
