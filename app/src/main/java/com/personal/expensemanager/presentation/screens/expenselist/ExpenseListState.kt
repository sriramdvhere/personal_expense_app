package com.personal.expensemanager.presentation.screens.expenselist

import com.personal.expensemanager.data.local.entity.ExpenseEntity
import java.time.LocalDate

data class GroupedExpenses(
    val date: LocalDate,
    val expenses: List<ExpenseEntity>,
    val dayTotal: Double
)

data class ExpenseListState(
    val groupedExpenses: List<GroupedExpenses> = emptyList(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isFilterApplied: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val expenseToDelete: ExpenseEntity? = null
)
