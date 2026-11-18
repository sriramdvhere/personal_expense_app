package com.personal.expensemanager.presentation.screens.expenselist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.expensemanager.ExpenseManagerApp
import com.personal.expensemanager.presentation.components.DateRangeFilter
import com.personal.expensemanager.presentation.components.EmptyState
import com.personal.expensemanager.presentation.components.ExpenseCard
import com.personal.expensemanager.presentation.components.LoadingIndicator
import com.personal.expensemanager.util.formatAmount
import java.time.format.DateTimeFormatter

private val DATE_HEADER_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as ExpenseManagerApp
    val viewModel: ExpenseListViewModel = viewModel(
        factory = ExpenseListViewModel.factory(app.container.repository)
    )
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("All Expenses") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                DateRangeFilter(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onStartDateSelected = viewModel::updateStartDate,
                    onEndDateSelected = viewModel::updateEndDate,
                    onApply = viewModel::applyFilter,
                    onClear = viewModel::clearFilter,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (state.groupedExpenses.isEmpty() && !state.isLoading) {
                    EmptyState(message = "No expenses found")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        state.groupedExpenses.forEach { group ->
                            item(key = "header_${group.date}") {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "📅 ${group.date.format(DATE_HEADER_FORMATTER)}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = formatAmount(group.dayTotal),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }

                            item(key = "expenses_${group.date}") {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    group.expenses.forEachIndexed { index, expense ->
                                        if (index > 0) {
                                            HorizontalDivider()
                                        }
                                        ExpenseCard(
                                            expense = expense,
                                            onEdit = { onNavigateToEdit(expense.id) },
                                            onDelete = { viewModel.requestDelete(expense) },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            LoadingIndicator()
        }
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) {
                    Text("Cancel")
                }
            }
        )
    }
}
