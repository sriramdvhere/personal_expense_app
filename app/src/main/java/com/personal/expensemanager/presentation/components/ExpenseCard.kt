package com.personal.expensemanager.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.model.Category
import com.personal.expensemanager.util.formatAmount

fun categoryIcon(category: Category): String = when (category) {
    Category.FOOD_DINING -> "🍔"
    Category.TRANSPORTATION -> "🚗"
    Category.SHOPPING -> "🛒"
    Category.BILLS_UTILITIES -> "📱"
    Category.ENTERTAINMENT -> "🎬"
    Category.HEALTH_MEDICAL -> "🏥"
    Category.EDUCATION -> "📚"
    Category.GROCERIES -> "🛍️"
    Category.RENT -> "🏠"
    Category.TRAVEL -> "✈️"
    Category.PERSONAL_CARE -> "💅"
    Category.GIFTS_DONATIONS -> "🎁"
    Category.INVESTMENTS -> "📈"
    Category.INSURANCE -> "🛡️"
    Category.OTHERS -> "📌"
}

@Composable
fun ExpenseCard(
    expense: ExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = categoryIcon(expense.category),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = expense.category.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (!expense.note.isNullOrBlank()) {
                Text(
                    text = expense.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = formatAmount(expense.amount),
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit expense"
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete expense"
            )
        }
    }
}
