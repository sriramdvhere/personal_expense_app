package com.personal.expensemanager.presentation.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.expensemanager.ExpenseManagerApp
import com.personal.expensemanager.presentation.components.CategoryBreakdownItem
import com.personal.expensemanager.presentation.components.ErrorMessage
import com.personal.expensemanager.presentation.components.ExpenseCard
import com.personal.expensemanager.presentation.components.LoadingIndicator
import com.personal.expensemanager.presentation.components.SummaryCard

@Composable
fun DashboardScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToList: () -> Unit,
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            (LocalContext.current.applicationContext as ExpenseManagerApp).container.repository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(
            message = uiState.error!!,
            modifier = Modifier.padding(16.dp)
        )
        else -> DashboardContent(
            uiState = uiState,
            onPrevMonth = viewModel::prevMonth,
            onNextMonth = viewModel::nextMonth,
            onNavigateToList = onNavigateToList
        )
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigateToList: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SummaryCard(
                yearMonth = uiState.selectedMonth,
                totalAmount = uiState.monthlyTotal,
                onPrevMonth = onPrevMonth,
                onNextMonth = onNextMonth,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(uiState.categoryBreakdown) { summary ->
                CategoryBreakdownItem(
                    summary = summary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                HorizontalDivider()
            }
        }

        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(uiState.recentTransactions) { expense ->
            ExpenseCard(
                expense = expense,
                onEdit = {},
                onDelete = {}
            )
            HorizontalDivider()
        }

        item {
            TextButton(
                onClick = onNavigateToList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All →")
            }
        }
    }
}
