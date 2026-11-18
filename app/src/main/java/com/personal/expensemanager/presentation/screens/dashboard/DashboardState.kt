package com.personal.expensemanager.presentation.screens.dashboard

import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.model.CategorySummary
import java.time.YearMonth

data class DashboardState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthlyTotal: Double = 0.0,
    val categoryBreakdown: List<CategorySummary> = emptyList(),
    val recentTransactions: List<ExpenseEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
