package com.personal.expensemanager.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.personal.expensemanager.data.repository.ExpenseRepository
import com.personal.expensemanager.model.CategorySummary
import com.personal.expensemanager.util.toEpochMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

class DashboardViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        loadData(_uiState.value.selectedMonth)
    }

    fun prevMonth() {
        val newMonth = _uiState.value.selectedMonth.minusMonths(1)
        _uiState.update { it.copy(selectedMonth = newMonth) }
        loadData(newMonth)
    }

    fun nextMonth() {
        val newMonth = _uiState.value.selectedMonth.plusMonths(1)
        _uiState.update { it.copy(selectedMonth = newMonth) }
        loadData(newMonth)
    }

    private fun loadData(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val startMillis = yearMonth.atDay(1).toEpochMillis()
                val endMillis = yearMonth.atEndOfMonth().toEpochMillis()

                val monthlyTotal = withContext(Dispatchers.IO) {
                    repository.getTotalByDateRange(startMillis, endMillis) ?: 0.0
                }
                val categoryTotals = withContext(Dispatchers.IO) {
                    repository.getCategoryBreakdown(startMillis, endMillis)
                }
                val recentTransactions = withContext(Dispatchers.IO) {
                    repository.getRecentExpenses(5)
                }
                val categoryBreakdown = categoryTotals
                    .filter { it.total > 0 }
                    .map { ct ->
                        val pct = if (monthlyTotal > 0) ct.total / monthlyTotal * 100 else 0.0
                        CategorySummary(ct.category, ct.total, pct)
                    }

                _uiState.update {
                    it.copy(
                        monthlyTotal = monthlyTotal,
                        categoryBreakdown = categoryBreakdown,
                        recentTransactions = recentTransactions,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun Factory(repository: ExpenseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DashboardViewModel(repository) as T
            }
    }
}
