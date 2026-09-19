package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.SavingsGoalEntity
import com.example.ui.theme.EmeraldPrimary
import java.util.Calendar

@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        targetAmount: Double,
        initialDeposit: Double,
        targetDateMillis: Long,
        icon: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var initialDepositText by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("headphones") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val icons = listOf("headphones", "shield", "flight", "savings", "car")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Savings Goal",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Set a target for something you are saving towards (e.g., 'New Headphone - ₹5,000').",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. New Headphones, Laptop, Trip") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_goal_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            targetAmountText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Target Amount (₹)") },
                    placeholder = { Text("e.g. 5000") },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_goal_target_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = initialDepositText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            initialDepositText = it
                        }
                    },
                    label = { Text("Starting Saved Amount (Optional)") },
                    placeholder = { Text("0") },
                    leadingIcon = { Text("₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    icons.forEach { iconKey ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) EmeraldPrimary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryUtils.getGoalIcon(iconKey),
                                contentDescription = iconKey,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetAmountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        errorMessage = "Please enter a goal title"
                        return@Button
                    }
                    if (target == null || target <= 0) {
                        errorMessage = "Please enter a valid target amount"
                        return@Button
                    }
                    val initial = initialDepositText.toDoubleOrNull() ?: 0.0
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 60) }

                    onSave(title.trim(), target, initial, cal.timeInMillis, selectedIcon)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_savings_goal_button")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DepositFundsDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onDeposit: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickPills = listOf(500.0, 1000.0, 2000.0, 5000.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Funds to ${goal.title}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Goal Target: ${CategoryUtils.formatCurrency(goal.targetAmount)}  •  Current: ${CategoryUtils.formatCurrency(goal.currentAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Quick increment pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPills.forEach { pillAmount ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    amountText = pillAmount.toInt().toString()
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = "+₹${pillAmount.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            amountText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Deposit Amount (₹)") },
                    placeholder = { Text("e.g. 1000") },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter an amount to deposit"
                        return@Button
                    }
                    onDeposit(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("confirm_deposit_button")
            ) {
                Text("Deposit Funds")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
