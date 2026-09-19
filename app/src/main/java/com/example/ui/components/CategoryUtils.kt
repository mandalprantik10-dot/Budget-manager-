package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryBills
import com.example.ui.theme.CategoryEntertainment
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryTransport
import com.example.ui.theme.EmeraldPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CategoryUtils {

    val PREBUILT_CATEGORIES = listOf(
        "Food & Dining",
        "Transport",
        "Shopping",
        "Bills & Utilities",
        "Entertainment",
        "Health",
        "Income",
        "Other"
    )

    fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "food & dining", "food", "dining", "groceries" -> CategoryFood
            "transport", "travel", "commute", "cab" -> CategoryTransport
            "shopping", "clothing", "electronics" -> CategoryShopping
            "bills & utilities", "bills", "utilities", "electricity", "internet" -> CategoryBills
            "entertainment", "movies", "games", "streaming" -> CategoryEntertainment
            "health", "medical", "pharmacy", "fitness" -> CategoryHealth
            "income", "salary", "freelance", "investment" -> EmeraldPrimary
            else -> CategoryOther
        }
    }

    fun getCategoryIcon(category: String): ImageVector {
        return when (category.lowercase()) {
            "food & dining", "food", "dining", "groceries" -> Icons.Default.Restaurant
            "transport", "travel", "commute", "cab" -> Icons.Default.DirectionsCar
            "shopping", "clothing", "electronics" -> Icons.Default.ShoppingCart
            "bills & utilities", "bills", "utilities", "electricity", "internet" -> Icons.Default.Receipt
            "entertainment", "movies", "games", "streaming" -> Icons.Default.Movie
            "health", "medical", "pharmacy", "fitness" -> Icons.Default.LocalHospital
            "income", "salary" -> Icons.Default.Paid
            "freelance", "work" -> Icons.Default.Work
            "investment" -> Icons.Default.TrendingUp
            else -> Icons.Default.Category
        }
    }

    fun getGoalIcon(iconKey: String): ImageVector {
        return when (iconKey.lowercase()) {
            "headphones" -> Icons.Default.Headphones
            "shield", "emergency" -> Icons.Default.Shield
            "flight", "travel", "trip" -> Icons.Default.Flight
            "savings", "vault" -> Icons.Default.Savings
            "car", "transport" -> Icons.Default.DirectionsCar
            else -> Icons.Default.Paid
        }
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        return "₹" + format.format(amount)
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
