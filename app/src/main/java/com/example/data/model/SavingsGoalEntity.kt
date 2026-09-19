package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long = System.currentTimeMillis() + (60L * 24 * 3600 * 1000), // ~2 months ahead
    val categoryIcon: String = "headphones",
    val isCompleted: Boolean = false
) {
    val progress: Float get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentage: Int get() = (progress * 100).toInt()
    val remainingAmount: Double get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}
