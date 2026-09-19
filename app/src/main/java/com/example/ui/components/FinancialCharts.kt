package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategorySpending
import com.example.data.model.TransactionEntity
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryExpenseDoughnutChart(
    categorySpendings: List<CategorySpending>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val nonZeroSpendings = remember(categorySpendings) {
        categorySpendings.filter { it.amount > 0 }
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(categorySpendings) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (nonZeroSpendings.isEmpty() || totalExpense <= 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses recorded for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Doughnut Canvas with Center Info
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 32.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val arcSize = Size(diameter, diameter)
                    val topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )

                    var currentStartAngle = -90f
                    val total = totalExpense.toFloat()

                    nonZeroSpendings.forEach { item ->
                        val sweep = ((item.amount.toFloat() / total) * 360f) * animationProgress.value
                        val color = CategoryUtils.getCategoryColor(item.category)

                        // Draw arc with subtle gap
                        if (sweep > 1f) {
                            drawArc(
                                color = color,
                                startAngle = currentStartAngle,
                                sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        currentStartAngle += sweep
                    }
                }

                // Center Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CategoryUtils.formatCurrency(totalExpense),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend FlowRow
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nonZeroSpendings.forEach { item ->
                    val color = CategoryUtils.getCategoryColor(item.category)
                    val percentage = if (totalExpense > 0) ((item.amount / totalExpense) * 100).toInt() else 0

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DailySpending(
    val dayLabel: String,
    val expense: Double,
    val income: Double
)

@Composable
fun SpendingTrendBarGraph(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    // Group transactions by the last 7 days
    val dailyData = remember(transactions) {
        val list = mutableListOf<DailySpending>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val year = cal.get(Calendar.YEAR)
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            val label = if (i == 0) "Today" else dayFormat.format(cal.time)

            val matchingTxs = transactions.filter { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.DAY_OF_YEAR) == dayOfYear
            }

            val expenseSum = matchingTxs.filter { it.isExpense }.sumOf { it.amount }
            val incomeSum = matchingTxs.filter { it.isIncome }.sumOf { it.amount }
            list.add(DailySpending(label, expenseSum, incomeSum))
        }
        list
    }

    val maxAmount = remember(dailyData) {
        val maxVal = dailyData.maxOfOrNull { maxOf(it.expense, it.income) } ?: 1000.0
        if (maxVal <= 0.0) 1000.0 else maxVal
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyData) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val coralColor = CoralExpense
    val emeraldColor = EmeraldPrimary
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val textLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last 7 Days Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(emeraldColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Income", style = MaterialTheme.typography.labelSmall, color = textLabelColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(coralColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expense", style = MaterialTheme.typography.labelSmall, color = textLabelColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Canvas Chart with Bars and Trend Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartHeight = size.height - 30.dp.toPx()
                val chartWidth = size.width
                val barCount = dailyData.size
                val step = chartWidth / barCount
                val barWidth = 14.dp.toPx()

                // Draw 3 horizontal grid guidelines
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val y = chartHeight * (i.toFloat() / gridSteps)
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Trend line path points
                val trendPoints = mutableListOf<Offset>()

                dailyData.forEachIndexed { index, item ->
                    val centerX = (index * step) + (step / 2f)

                    // Expense Bar
                    val expenseRatio = (item.expense / maxAmount).toFloat().coerceIn(0f, 1f)
                    val expenseBarHeight = (expenseRatio * chartHeight) * animationProgress.value
                    if (expenseBarHeight > 0) {
                        val topLeft = Offset(centerX - barWidth - 2.dp.toPx(), chartHeight - expenseBarHeight)
                        drawRoundRect(
                            color = coralColor,
                            topLeft = topLeft,
                            size = Size(barWidth, expenseBarHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                    }

                    // Income Bar
                    val incomeRatio = (item.income / maxAmount).toFloat().coerceIn(0f, 1f)
                    val incomeBarHeight = (incomeRatio * chartHeight) * animationProgress.value
                    if (incomeBarHeight > 0) {
                        val topLeft = Offset(centerX + 2.dp.toPx(), chartHeight - incomeBarHeight)
                        drawRoundRect(
                            color = emeraldColor,
                            topLeft = topLeft,
                            size = Size(barWidth, incomeBarHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                    }

                    // Collect point for expense trend line
                    trendPoints.add(Offset(centerX, chartHeight - expenseBarHeight))
                }

                // Draw Smooth Line connecting expense peaks if animated
                if (trendPoints.size > 1 && animationProgress.value > 0.5f) {
                    val linePath = Path().apply {
                        moveTo(trendPoints[0].x, trendPoints[0].y)
                        for (k in 1 until trendPoints.size) {
                            val prev = trendPoints[k - 1]
                            val curr = trendPoints[k]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = coralColor.copy(alpha = 0.5f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // X-Axis Day Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dailyData.forEach { item ->
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
