package com.personal.expensemanager.presentation.screens.expenselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.data.repository.ExpenseRepository
import com.personal.expensemanager.util.toEpochMillis
import com.personal.expensemanager.util.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ExpenseListViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListState())
    val uiState: StateFlow<ExpenseListState> = _uiState.asStateFlow()

    init {
        loadAllExpenses()
    }

    private fun loadAllExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val expenses = withContext(Dispatchers.IO) {
                    repository.getAllExpenses()
                }
                _uiState.update { it.copy(groupedExpenses = groupByDate(expenses), isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun groupByDate(expenses: List<ExpenseEntity>): List<GroupedExpenses> {
        return expenses
            .groupBy { it.date.toLocalDate() }
            .entries
            .sortedByDescending { it.key }
            .map { (date, entries) ->
                GroupedExpenses(
                    date = date,
                    expenses = entries,
                    dayTotal = entries.sumOf { it.amount }
                )
            }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun applyFilter() {
        val startDate = _uiState.value.startDate
        val endDate = _uiState.value.endDate
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val expenses = withContext(Dispatchers.IO) {
                    repository.getExpensesByDateRange(
                        startMillis = startDate?.toEpochMillis() ?: 0L,
                        endMillis = endDate?.toEpochMillis() ?: Long.MAX_VALUE
                    )
                }
                _uiState.update {
                    it.copy(groupedExpenses = groupByDate(expenses), isLoading = false, isFilterApplied = true)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun clearFilter() {
        _uiState.update { it.copy(startDate = null, endDate = null, isFilterApplied = false) }
        loadAllExpenses()
    }

    fun requestDelete(expense: ExpenseEntity) {
        _uiState.update { it.copy(showDeleteConfirmation = true, expenseToDelete = expense) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(showDeleteConfirmation = false, expenseToDelete = null) }
    }

    fun confirmDelete() {
        val expense = _uiState.value.expenseToDelete ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.delete(expense) }
                _uiState.update { it.copy(showDeleteConfirmation = false, expenseToDelete = null) }
                reloadCurrentFilter()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, showDeleteConfirmation = false, expenseToDelete = null) }
            }
        }
    }

    private fun reloadCurrentFilter() {
        if (_uiState.value.isFilterApplied) {
            applyFilter()
        } else {
            loadAllExpenses()
        }
    }

    companion object {
        fun factory(repository: ExpenseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(klass: Class<T>): T =
                    ExpenseListViewModel(repository) as T
            }
    }
}
