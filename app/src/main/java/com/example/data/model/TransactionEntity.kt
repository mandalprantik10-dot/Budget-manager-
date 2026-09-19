package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val isExpense: Boolean get() = type == TransactionType.EXPENSE.name
    val isIncome: Boolean get() = type == TransactionType.INCOME.name
}
